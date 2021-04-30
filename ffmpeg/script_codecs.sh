#!/bin/bash

codec_list="mjpeg mpeg1video mpeg4 h264" # Lista de codecs a utilizar
functs_list="sad sse satd chroma" # Lista de opciones de comparacion
algorithm_list="dia hex umh esa"


case "$1" in
	-h|--help)
	echo "Para hacer uso de este script existen las siguientes opciones:"
	echo "-ex [3|5|6|7|8|9] Numero de ejercicio a ejecutar"
	echo "Para algunos ejercicios es posible que se pidan mas parametros"
	echo "Para el ejercicio 7 existe una opcion para ver el peso de los ficheros"
	echo "-ex 7 size"
	exit 0
	;;
	-ex)		
		# Usado para las preguntas 3 y 4
		if [[ "$2" == "3" ]]; then		
			for codec in $codec_list; do 
				echo ${codec}
 				[ -f cub_${codec}.avi ] && rm cub_${codec}.avi
 				ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -codec:v ${codec} cub_${codec}.avi > ${codec}.out 2>&1
 				cat ${codec}.out | grep bench
			done
		# Usado para la pregunta 5
		elif [[ "$2" == "5" ]]; then
			for gop in {1..100..1}; do
				echo "GOP: ${gop}"
				[ -f exercici5/cubo_h264_g${gop}_ref0_r25.avi ] && rm exercici5/cubo_h264_g${gop}_ref0_r25.avi
 				ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -g $gop -refs 0 -r 25 -codec:v h264 exercici5/cubo_h264_g${gop}_ref0_r25.avi > exercici5/h264${gop}.out 2>&1
 				cat exercici5/h264${gop}.out | grep 'frame I:'
 				cat exercici5/h264${gop}.out | grep 'frame B:' 
 				cat exercici5/h264${gop}.out | grep 'frame P:' 
 			done

 		# Usado para la pregunta 6
 		elif [[ "$2" == "6" ]]; then
			for ra in {5..25..1}; do
				echo "FrameRate: ${ra}"
				[ -f exercici6/cubo_h264_g100_ref0_r${ra}.avi ] && rm exercici6/cubo_h264_g100_ref0_r${ra}.avi
 				ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -g 100 -refs 0 -r ${ra} -codec:v h264 exercici6/cubo_h264_g100_ref0_r${ra}.avi > exercici6/h264${ra}.out 2>&1
 				FILESIZE=$(stat -c%s exercici6/cubo_h264_g100_ref0_r${ra}.avi )
 				echo "$FILESIZE bytes"
 			done

		elif [[ "$2" == "7" ]]; then
			if [[ "$3" == "" ]]; then
				# Usado para las preguntas del 7
				for codec in $codec_list; do 		
					for funct in $functs_list; do
						echo ${codec} - ${funct}
						[ -f cmp-functs/cub_${codec}_${funct}.avi ] && rm cmp-functs/cub_${codec}_${funct}.avi
						ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -cmp ${funct} -codec:v ${codec} cmp-functs/cub_${codec}_${funct}.avi > cmp-functs/${codec}_${funct}.out 2>&1
						cat cmp-functs/${codec}_${funct}.out | grep bench
					done
				done
			elif [[ "$3" == "size" ]]; then				
				for codec in $codec_list; do 		
					for funct in $functs_list; do
					du -sh cmp-functs/cub_${codec}_${funct}.avi
					done
				done
			fi

		elif [[ "$2" == "8" ]]; then	
			# Usado para las preguntas del 8
			for codec in $codec_list; do 		
				#codec="h264" #testing
				for algo in $algorithm_list; do
					echo "${codec} - ${algo}"
					[ -f algorithm-change/cub_${codec}_${algo}.avi ] && rm algorithm-change/cub_${codec}_${algo}.avi
					ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -me_method ${algo} -codec:v ${codec} algorithm-change/${codec}_${algo}.avi > algorithm-change/${codec}_${algo}.out 2>&1
					cat algorithm-change/${codec}_${algo}.out | grep bench
					FILESIZE=$(stat -c%s algorithm-change/${codec}_${algo}.avi)
 					echo "$FILESIZE bytes"
				done
			done

		elif [[ "$2" == "9" ]]; then
			# Usado para las preguntas del 9		
			for dis in {0..10000..1000}; do
				echo "h264 umh - ${dis}"
				[ -f exercici9/cub_h264_umh_dis${dis}.avi ] && rm exercici9/cub_h264_umh_dis${dis}.avi
				ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -me_method umh -me_range ${dis} -codec:v h264 exercici9/cub_h264_umh_dis${dis}.avi > exercici9/cub_h264_umh_dis${dis}.out 2>&1
				cat exercici9/cub_h264_umh_dis${dis}.out | grep bench
				FILESIZE=$(stat -c%s exercici9/cub_h264_umh_dis${dis}.avi)
 				echo "$FILESIZE bytes"

			done
		
		elif [[ "$2" == "10" ]]; then
			echo "h264 GOP 100 R 60 UMH SATD hex 1024 "
			[ -f exercici10/cub_h264_perf.avi ] && rm exercici10/cub_h264_perf.avi
			ffmpeg -benchmark -r 60 -i ./Cubo/Cubo%02d.png -g 100 -cmp satd -me_method hex -me_range 1024 -codec:v h264 exercici10/cub_h264_perf.avi > exercici10/cub_h264_perf.out 2>&1
			cat exercici10/cub_h264_perf.out | grep bench
			FILESIZE=$(stat -c%s exercici10/cub_h264_perf.avi)
 			echo "$FILESIZE bytes"

		fi		
	;;
	*)
		echo "Sin parametros"
    	exit 0
    ;;
esac