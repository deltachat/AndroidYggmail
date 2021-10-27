#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

function quit {
    echo -e "${RED}Task failed. $1 ${NC}"
    exit 1
}

function showtitle {
    echo -e "\n\n${GREEN}--- $1 ...${NC}\n"
}

# -------- install golang ---------------
showtitle "Installing golang"

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        GO_VERSION=go1.16.6.linux-amd64
        EXPECTED_FP=be333ef18b3016e9d7cb7b1ff1fdb0cac800ca0be4cf2290fe613b3d069dfe0d
elif [[ "$OSTYPE" == "darwin"* ]]; then
        # Mac OSX
        GO_VERSION=go1.16.6.darwin-amd64
        EXPECTED_FP=e4e83e7c6891baa00062ed37273ce95835f0be77ad8203a29ec56dbf3d87508a
else
        echo "$OSTYPE is currently not supported."
        exit 1
fi


if [[ $(ls -A ${GO_VERSION}.tar.gz) ]]; then
    echo "Reusing downloaded golang bundle"
else
    echo "Installing go lang bundle ${GO_VERSION}.tar.gz from https://golang.org/dl/$GO_VERSION.tar.gz"
    curl -L -o "${GO_VERSION}.tar.gz" "https://golang.org/dl/${GO_VERSION}.tar.gz"
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
      ACTUAL_FP=$(sha256sum $GO_VERSION.tar.gz | cut -d " " -f1)
    else
      ACTUAL_FP=$(shasum -a 256 $GO_VERSION.tar.gz | cut -d " " -f1)
    fi

    if [[ ! $ACTUAL_FP == "$EXPECTED_FP" ]]; then
        quit "Download seems to be corrupted. Cancelling build. \n actual FP:   $ACTUAL_FP \n expected FP: $EXPECTED_FP"
    fi

fi

if [[ ! -d ./golang ]]; then
    mkdir ./golang
fi

yes | tar -C ./golang -xzf $GO_VERSION.tar.gz || quit "Could not untar $GO_VERSION.tar.gz"


# -------- update submodules ---------------
showtitle "Updating submodules"
git submodule sync --recursive || quit "git submodule sync --recursive"
git submodule update --init --recursive || quit "git submodule update --init --recursive"


# -------- init environment variables ---------------
cd ./golang || quit "cd ./golang"
export GOPATH=$(pwd)
export GO_LANG=$(pwd)/go/bin
export GO_COMPILED=$(pwd)/bin
export PATH="${GO_LANG}:${GO_COMPILED}:${PATH}"
# go env
cd ..

# -------- init gomobile ---------------
showtitle "Getting gomobile"
./golang/go/bin/go get golang.org/x/mobile/cmd/gomobile || quit "./golang/go/bin/go get golang.org/x/mobile/cmd/gomobile@latest"

showtitle "Initiating gomobile"
cd ../yggmail || quit "cd ../yggmail"
../lib-yggmail/golang/bin/gomobile init || quit "./golang/bin/gomobile init"

# -------- remove old libs -------------
showtitle "Removing old yggmail libs"
cd ../lib-yggmail || quit "cd ../lib-yggmail"
if [[ -d ./libs ]]; then
    if [[ $(ls -A ./libs/*) ]]; then
        rm -rf ./libs/*
    fi
else
    mkdir ./libs
fi

# -------- build yggmail ---------------
showtitle "Building yggmail core for all architectures"
cd ../yggmail || quit "cd ../yggmail"
../lib-yggmail/golang/bin/gomobile bind -target='android' -o ../lib-yggmail/libs/yggmail.aar -v ./mobile/yggmail/
