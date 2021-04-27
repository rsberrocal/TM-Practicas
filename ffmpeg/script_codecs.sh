#!/bin/bash

codec_list="mjpeg mpeg1video mpeg4 h264" # Lista de codecs a utilizar
functs_list="sad sse satd chroma" # Lista de opciones de comparacion


case "$1" in
	-h|--help)
	echo "Para hacer uso de este script existen las siguientes opciones:"
	echo "-ex [3|5|6|7] Numero de ejercicio a ejecutar"
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
				echo ${gop}
				[ -f cub_h264.avi ] && rm cub_h264.avi
 				ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -g $gop -refs 0 -r 25 -codec:v h264 ./cubo_h264_g${gop}_ref0_r25.avi
 				cat $h264.out | grep 'frame I:'
 			done

 		# Usado para la pregunta 6
 		elif [[ "$2" == "6" ]]; then
			for ra in {5..25..1}; do
				echo ${ra}
				[ -f cub_h264.avi ] && rm cub_h264.avi
 				ffmpeg -benchmark -r 25 -i ./Cubo/Cubo%02d.png -g 100 -refs 0 -r ${ra} -codec:v h264 ./cubo_h264_g100_ref0_r${ra}.avi
 				cat $h264.out | grep bench
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
		fi		
	;;
	*)
		echo "Sin parametros"
    	exit 0
    ;;
esac

# Usado para las preguntas 5 y 6

