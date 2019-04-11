#!/bin/bash

if [ $# -ne 2 ];
then
    >&2 echo "Usage: $0 FILE IMAGE"
    exit 1
fi

sed 's@image":.*@image": '\"$2\",'@' $1 > $1.new && mv $1.new $1
