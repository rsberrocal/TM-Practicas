import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarOutputStream;

public class Encoder {
    public final static String BUILDER_FNAME = "builder_skeleton.gz";
    public final static String VIDEO_FNAME = "video.zip";
    public final static String COMPRESSED_FNAME = "compressed.tar";

    private ArrayList <Byte> builder;
    private ArrayList<BufferedImage> raw;
    private final int cols;
    private final int rows;
    private final float quality;
    private final short gop;
    private final short brick;    // breaking block size
    private final short offset;   // Defines Block neighbourhood to seek/match

    public Encoder(ArrayList video, float quality, short gop, short block_size, short offset){
        this.raw = video;
        this.quality = quality;
        this.gop = gop;
        this.brick = block_size;
        this.rows = raw.get(0).getWidth()/brick;
        this.cols = raw.get(0).getHeight()/brick;
        this.offset = offset;
    }

    private BufferedImage[] chunkFrame(BufferedImage frame){
        int chunks = rows * cols;
        int count = 0;

        BufferedImage imgs[] = new BufferedImage[chunks]; //Image array to hold image chunks
        for (int y = 0; y < frame.getHeight(); y+=this.brick) {
            for (int x = 0; x < frame.getWidth(); x+=this.brick) {
                imgs[count] = frame.getSubimage(x, y, this.brick, this.brick);
                count++;

            }
        }
        return imgs;
    }

    private double compareImages(BufferedImage im1, BufferedImage im2){
        assert(im1.getHeight() == im2.getHeight() && im1.getWidth() == im2.getWidth());
        double variation = 0.0;
        for(int y = 0;y < im1.getHeight();y++){
            for(int x = 0;x < im1.getWidth();x++){
                variation += compareARGB(im1.getRGB(x,y),im2.getRGB(x,y))/Math.sqrt(3);
            }
        }
        return variation/(im1.getWidth()*im1.getHeight());
    }

    private double compareARGB(int rgb1, int rgb2){
        double r1 = ((rgb1 >> 16) & 0xFF)/255.0;
        double r2 = ((rgb2 >> 16) & 0xFF)/255.0;
        double g1 = ((rgb1 >> 8) & 0xFF)/255.0;
        double g2 = ((rgb2 >> 8) & 0xFF)/255.0;
        double b1 = (rgb1 & 0xFF)/255.0;
        double b2 = (rgb2 & 0xFF)/255.0;
        double a1 = ((rgb1 >> 24) & 0xFF)/255.0;
        double a2 = ((rgb2 >> 24) & 0xFF)/255.0;

        // if there is transparency, the alpha values will make difference smaller
        return a1*a2*Math.sqrt((r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2));
    }

    private void serializeShort(short n){
        this.builder.add((byte)(n & 0xff));
        this.builder.add((byte)((n >> 8) & 0xff ));
    }

    private void serializeCoord(short x, short y){
        serializeShort(x);
        serializeShort(y);
    }

    public void saveZip(File file){
        // guardar en zip
    }

    private double meanValue(BufferedImage image){
        Raster raster = image.getRaster();
        double sum = 0.0;

        for(int y = 0; y < image.getHeight(); ++y)
            for(int x = 0; x < image.getWidth(); ++x)
                sum += raster.getSample(x,y,0);

        return sum / (image.getWidth() * image.getHeight() );
    }

    private boolean templateMatching(int h, BufferedImage template, BufferedImage pframe,
                                     ArrayList <int[]> coords){

        int xmin, xmax, ymin, ymax;
        ymin = (h%rows)*brick - offset;
        if( ymin < 0 ) ymin = 0;

        ymax = brick *((h%rows) + 1) + offset;
        if( ymax  > raw.get(0).getWidth()) ymax = raw.get(0).getWidth();

        xmin = (h/rows)*brick - offset;
        if( xmin < 0 ) xmin = 0;

        xmax = brick * ((h/rows)+1) + offset;
        if( xmax > raw.get(0).getHeight()) xmax = raw.get(0).getHeight();

        //System.out.println("Brick "+h+": Coordenades: of search"+xmin+" "+xmax+" "+ymin+" "+ymax);

        for(int i = ymin; i <= ymax-brick; i++){
            for(int j = xmin; j <= xmax-brick; j++){
                /* matching evaluation */

                double corr = compareImages( pframe.getSubimage(i, j, this.brick, this.brick), template);
                if(corr < this.quality){
                    //System.out.println("\t\tINDEXED brick @("+i+","+j+") with corr:"+corr );
                    int[] coord = { i, j };
                    coords.add(coord);
                    serializeCoord((short)i, (short)j);
                    serializeShort((short)h);
                    //System.out.println("\t\t(x, y) " + coord[0] + ", " + coord[1] + " / h " + h);
                    return true;
                }
            }
        }
        return false;
    }

    private void setPatchColor(BufferedImage pframe, ArrayList <int[]> coords ){
        int x, y;
        for (int[] coord : coords) {
            x = coord[0];
            y = coord[1];
            // Compute average patch color
            int[] colors = pframe.getRGB(x, y, this.brick, this.brick, null, 0, this.brick);
            int r = 0;
            int b = 0;
            int g = 0;
            for (int c : colors){
                r += ((c >> 16) & 0xFF);
                g += ((c >> 8) & 0xFF);
                b += (c & 0xFF);
            }
            int R = r / colors.length;
            int G = g / colors.length;
            int B = b / colors.length;
            //System.out.println("\tAVG PATCH RGB: " + R + " , " + G + " , " + B);

            // Set color patch
            int[] rgbArray = new int[(this.brick-2) * (this.brick-2)];
            Color c = new Color(R,G,B);
            //Color c = new Color(255,255,255);
            Arrays.fill(rgbArray, c.getRGB());
            pframe.setRGB(++x, ++y, this.brick-2, this.brick-2, rgbArray, 0, 0);
        }
    }

////////////////////////////////////////////////////
//               GZIP compression                 //
////////////////////////////////////////////////////

    private boolean compressGzipFile( byte[] data ){
        try{
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
            try{
                GZIPOutputStream gos;
                gos = new GZIPOutputStream(byteStream, false);
                try{
                    gos.write(data, 0, data.length);
                } finally { gos.close(); }
            } finally { byteStream.close(); }

            byte[] compressedData = byteStream.toByteArray();
            FileOutputStream fileStream = new FileOutputStream(BUILDER_FNAME);
            try{ fileStream.write(compressedData); } finally {
                try{ fileStream.close(); }catch(Exception e){
                    /* We should probably delete the file now? */
                    return false;
                }
            }
        } catch(IOException ex){
            return false;
        }
        return true;
    }

    private byte[] toByteArray(ArrayList<Byte> in) {
        int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) ret[i] = in.get(i);
        return ret;
    }

    private void tar(File[] filesToTar) throws FileNotFoundException {
        // Output file stream
        FileOutputStream dest = new FileOutputStream( COMPRESSED_FNAME );

        // Create a TarOutputStream
        TarOutputStream out = new TarOutputStream( new BufferedOutputStream( dest ) );
        try{
            for(File f : filesToTar){
                out.putNextEntry(new TarEntry(f, f.getName()));
                BufferedInputStream origin = new BufferedInputStream(new FileInputStream( f ));

                int count;
                byte data[] = new byte[2048];
                while((count = origin.read(data)) != -1) {
                    out.write(data, 0, count);
                }

                out.flush();
                origin.close();
                f.delete();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
/*
-----------------------------------------------------------------------------------------------------------
 */
    public void encode(){
        short matches;
        ArrayList <int[]> coords;
        BufferedImage[] iframe = null;


        this.builder = new ArrayList();
        // save builder skeleton header:
        this.builder.add( (byte) (this.brick & 0xff) );
        this.builder.add( (byte) (this.gop & 0xff) );
        this.builder.add( (byte) (this.rows & 0xff) );

        System.out.println("@encode STARTING (" + raw.size()+ " frames)...");
        //loop over all video frames
        for (int k = 0; k < raw.size(); k++) {
            matches = 0;

            BufferedImage pframe = raw.get(k);

            // We only take as a reference frame the first picture from every GOP
            if( k % this.gop == 0 ){
                // Imágenes codificadas intracuadro (origen)
                iframe = chunkFrame(pframe);
                System.out.println("-> New _IFRAME " + k + " splitted into " + iframe.length + " chunks" );
            } else {
                // Imágenes codificadas intercuadro (destino)
                coords = new ArrayList();
                this.builder.add( (byte) (k & 0xff) );
                System.out.print("\t_PFRAME "+ k + " ..." );

                this.builder.add((byte) 0);
                this.builder.add((byte) 0);
                int m_index = this.builder.size() - 2;
                /* we get the m_index as a pointer as it reserves the needed bytes positions in the array,
                Once we have computed matches, we can update it directly */

                //template-matching for each block of IFRAME into PFRAME
                for (int h=0; h < iframe.length; h++) {
                    //System.out.println("\t\t@debug tenplate matching iteration "+ h );
                    if( templateMatching(h, iframe[h], pframe, coords) ) matches++;
                }

                System.out.println("\t\t-> total matches ["+ matches +"]");
                if(matches > 0){
                    //Let's update matches field with a valid value
                    this.builder.set(m_index, (byte)(matches & 0xff));
                    this.builder.set(m_index+1, (byte)((matches >> 8) & 0xff ));

                    setPatchColor(pframe, coords );
                }
            }
        }
        System.out.println("@encode FINISHED (" + raw.size()+ "frames)");

        //builder GZIP compression:
        if(!compressGzipFile( toByteArray(this.builder) )){
            System.out.println("GZIP compression failed!");
            return;
        }
        System.out.println("[Builder GZIP compression succeded]");

        //average filtering:
        System.out.println("Aplicant filtre d'average...");
        //aplicar filtro average.

        // Files to tar
        File[] files = new File[2];
        files[0] = new File(VIDEO_FNAME);
        files[1] = new File(BUILDER_FNAME);

        //images JPEG compression:
        saveZip(files[0]);

        //pack GZIP builder & compressed JPEG video togheter
        try {
            tar(files);
        } catch (FileNotFoundException ex) {
            System.out.println("A problem happened while generating tar");
           ex.printStackTrace();
        }

    }






}
