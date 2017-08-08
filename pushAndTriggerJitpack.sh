#!/usr/bin/env bash

REF=$(git rev-parse head | xargs echo -n)
echo $REF
echo -n ${REF} | pbcopy
echo "ref copied to clipboard"
git push
curl https://jitpack.io/api/builds/com.github.gmjonker/util/$REF
