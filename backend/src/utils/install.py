import pip
import os

from subprocess import STDOUT, check_call

def install(package):
    args = ['install', package]
    pip.main(args)

def install_apt_get(packages):
    check_call(['apt-get', 'install', '-y', packages], stdout=open(os.devnull,'wb'), stderr=STDOUT) 

if __name__ == '__main__':
    install("cherrypy")
    install("jinja2")
    install("pymongo")
    
    install_apt_get("python-dev libffi-dev libssl-dev")
    install("pyOpenSSL")