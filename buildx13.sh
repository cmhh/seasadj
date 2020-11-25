apt update && apt install gfortran make
mkdir -p /tmp/x13
mkdir -p ${HOME}/.local/bin
cd /tmp/x13
wget https://www.census.gov/ts/x13as/unix/x13ashtmlsrc_V1.1_B39.tar.gz
tar -xvf x13ashtmlsrc_V1.1_B39.tar.gz 
make -j20 -f makefile.gf 
mv x13asHTMLv11b39 ${HOME}/.local/bin/x13ashtml
echo "export PATH=$PATH:$HOME/.local/bin" >> ${HOME}/.bashrc
source ${HOME}/.bashrc
