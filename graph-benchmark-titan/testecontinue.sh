#!/bin/sh

NUMS="1 2 3 4 5 6 7"

for NUM in $NUMS
do
	echo $NUM

	Q=`expr $NUM % 2`
	while [ $Q -eq 0 ]
	do
		echo "even number"
#		continue
	done

	echo "odd number"
done
