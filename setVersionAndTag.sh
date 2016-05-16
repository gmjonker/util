#!/usr/bin/env bash

TAGFILE=src/main/resources/gitInfo.txt

if [ $# -lt 1 ]
    then
        echo
        echo Usage:
        echo
        echo ./setVersionAndTag.sh X.Y.Z "Description of tag"
        echo
        exit -1
fi

echo "Writing tag name and message to tag file..."
echo "Tag: v$1 $2" > $TAGFILE
echo "Last commit: " >> $TAGFILE
git log -1 >> $TAGFILE

echo
echo "Committing..."
git add -A
git commit -m "Set tag to $1 ($2)"

echo
echo
echo "Tagging v$1..."
git tag -a "v$1" -m "$2"

echo Done.
