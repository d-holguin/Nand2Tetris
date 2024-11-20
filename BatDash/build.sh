#!/bin/bash

# Navigate one directory up where jack compiler is. Note the bat script of the jack compiler provided by the nand2tetris coruse is expected to be 1 level up from this nested project.
# the compiled code is left in the build dir
cd .. || { echo "Failed to change directory"; exit 1; }

# Create the build folder if it doesn't exist
mkdir -p ./bat-dash/build || { echo "Failed to create build directory"; exit 1; }

# Copy all files from src to build
cp -r ./bat-dash/src/* ./bat-dash/build/ || { echo "Failed to copy files to build directory"; exit 1; }


# List contents to verify copying
echo "Contents of build directory:"
ls ./bat-dash/build

# Run JackCompiler.bat on the build directory
./JackCompiler.bat ./bat-dash/build || { echo "Failed to run JackCompiler"; exit 1; }

# Remove jack files
rm -r ./bat-dash/build/*.jack


echo "Finish Building"
ls ./bat-dash/build