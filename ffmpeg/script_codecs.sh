#!/bin/bash

codec_list="mjpeg mpeg1video mpeg4 h264" # Lista de codecs a utilizar
functs_list="sad sse satd chroma" # Lista de opciones de comparacion


case "$1" in
	-h|--help)
	echo "Para hacer uso de este script existen las siguientes opciones:"
	echo "-ex [3|7] Numero de ejercicio a ejecutar"
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
 				ffmpeg -benchmark -r 25 -i ./imagenes/Cubo%02d.png -codec:v ${codec} cub_${codec}.avi > ${codec}.out 2>&1
 				cat ${codec}.out | grep bench
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

