mkdir -p /tmp/x13
mkdir -p ${HOME}/.local/bin
cd /tmp/x13 && \
wget https://www.census.gov/ts/x13as/unix/x13ashtmlall_V1.1_B39.tar.gz 
tar -xvf x13ashtmlall_V1.1_B39.tar.gz 
mv x13ashtml ${HOME}/.local/bin
echo "export PATH=$PATH:$HOME/.local/bin" >> ${HOME}/.bashrc
source ${HOME}/.bashrc
