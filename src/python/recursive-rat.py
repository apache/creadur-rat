#
# Licensed to the Apache Software Foundation (ASF) under one   
# or more contributor license agreements.  See the NOTICE file 
# distributed with this work for additional information        
# regarding copyright ownership.  The ASF licenses this file   
# to you under the Apache License, Version 2.0 (the            
# "License"); you may not use this file except in compliance   
# with the License.  You may obtain a copy of the License at   
#                                                              
#   http://www.apache.org/licenses/LICENSE-2.0                 
#                                                              
# Unless required by applicable law or agreed to in writing,   
# software distributed under the License is distributed on an  
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       
# KIND, either express or implied.  See the License for the    
# specific language governing permissions and limitations      
# under the License.                                           
#

from sys import argv
from os import popen
from os import popen4
from os.path import isdir
from os.path import walk
from os.path import split
from os.path import splitext
from os.path import exists
from tempfile import mkdtemp
from os.path import join
from os import listdir
from os import makedirs
from shutil import rmtree
import md5
import sha

class UserAbortError(Exception):
    def __str__(self):
        return "Aborted by user. Bye."

def matchExtension(file, dir, extension):
    matches = []
    extension = '.' + extension.lower()
    for element in listdir(dir):
        root, ext = splitext(element)
        if extension == ext.lower() and root == file:
            matches.append(element)
    return matches

def printMatches(file, dir, extension):
    matches = matchExtension(file, dir, extension)
    for match in matches:
        matchFile = open(join(dir,match), 'r')
        text = matchFile.read(); 
        matchFile.close()
        print text
    

def untgz(file, dir):
    popen("tar -xzf " + file + " -C " + dir)
    
def unzip(file, dir):
    popen("unzip -d " + dir + " " + file)

def checkSums(filename, dir):
    file = open(join(dir, filename), 'r')
    contents = file.read()
    file.close()
    md5sum = md5.new()
    md5sum.update(contents)
    print "MD5:"
    print md5sum.hexdigest()
    printMatches(filename, dir, "MD5")
    shaSum = sha.new()
    shaSum.update(contents)
    print "SHA1:"
    print shaSum.hexdigest()
    printMatches(filename, dir, "SHA")
    printMatches(filename, dir, "SHA1")

def checkSignature(filename, dir, gpgCommand):
    file = join(dir, filename)
    signatureFile = file + '.asc'
    if not(exists(signatureFile)):
       signatureFile = file + '.sig'
       if not(exists(signatureFile)):
           print "SIGNATURE IS MISSING"
    command = gpgCommand + ' --verify ' + signatureFile + ' ' + file
    input, output = popen4(command)
    print output.read()

def rat(file, dir, uncompress, ratCommand, baseReportDirectory):
    if baseReportDirectory == None:
        reportdir = mkdtemp()
    else:
        reportdir = join(baseReportDirectory, file.replace('.', '_'))
        if exists(reportdir):
           rmtree(reportdir) 
        makedirs(reportdir)
            
    uncompress(join(dir, file), reportdir)
    command = ratCommand + " " + reportdir
    input, output = popen4(command)
    out = output.read()
    if not baseReportDirectory == None:
        f = open(join(baseReportDirectory, file + '.rat'), 'w')
        f.write(out)
        f.close()
    print out

def checkArchive(file, dir, uncompress, ratCommand, gpgCommand, baseReportDirectory):
    print "."
    print "."
    print "."
    print "###################################################"
    print "Checking ", file
    checkSums(file, dir)
    checkSignature(file, dir, gpgCommand)
    rat(file, dir, uncompress, ratCommand, baseReportDirectory)
    print "###################################################"
    print "."
    print "."
    print "."
        
def checkFile(file, dir, ratCommand, gpgCommand, baseReportDirectory):
    '''Checks the given file'''
    if file.endswith('.jar'):
        print "Checking jar ", file
        checkArchive(file, dir, lambda a,b:unzip(a,b), ratCommand, gpgCommand, baseReportDirectory)
    elif file.endswith('.zip'):
        print "Checking zip ", file
        checkArchive(file, dir, lambda a,b:unzip(a,b), ratCommand, gpgCommand, baseReportDirectory)
    elif file.endswith('.tar.gz') or file.endswith('.tgz'):
        print "Checking tgz ", file
        checkArchive(file, dir, lambda a,b:untgz(a,b), ratCommand, gpgCommand, baseReportDirectory)

def ignoreDirectory(dir):
    '''Is this directory to be ignored?'''
    if dir == "":
        return False
    else:
        head, tail = split(dir)
        if tail.startswith('.') and not(tail.startswith('..')):
            return True
        elif tail == "":
            return False
        else:
            return ignoreDirectory(head)

def checkDirectory(ratCommand, gpgCommand, dir, files, baseReportDir, check=lambda a,b,c,d: lambda x: checkFile(x,a,b,c,d)):
    '''Checks a directory'''
    if not(ignoreDirectory(dir)):
       print 'Checking directory ', dir
       map(check(dir, ratCommand, gpgCommand, baseReportDir), files)

def walkDirectories(base, ratCommand, gpgCommand, baseReportDir = None, f=lambda a,b,c,d,e: checkDirectory(a,b,c,d,e)):
    '''Traverses directories under base and executes checks'''
    print 'Starting traversal at ', base 
    walk(base, lambda dummy, dir, files: f(ratCommand, gpgCommand, dir, files, baseReportDir), None)
    
def enterRatCommand(rat = None):
    '''If necessary, prompts user for an acceptable Rat command'''
    if rat == None:
        try:
            rat = raw_input("Please enter Rat command (including path): ")
        except EOFError:
            raise UserAbortError()
        
        if rat == "":
            print "Rat command is required"
            rat = enterRatCommand()       
             
    ratResult = popen(rat).close()
    if not(ratResult == None):
        print "Cannot execute " + rat
        rat = enterRatCommand()
    return rat
    
def enterBaseDirectory(base = None):
    '''If necessary prompts user for an acceptable base directory'''
    if base == None:
        try:
            base = raw_input("Please enter base directory for recursive traversal: ")
        except EOFError:
            raise UserAbortError()
    if not(isdir(base)):
        print "'" + base + "' is not a directory."
        base = enterBaseDirectory()
    return base

def enterGPGCommand(gpgCommand = None):
    '''If necessary prompts user for the command used to test signatures'''
    if gpgCommand == None:
        try:
            gpgCommand = raw_input("GnuPG is required. Please enter GPG command: ")
        except EOFError:
            raise UserAbortError()
    result = popen(gpgCommand + " --version").close()
    if not(result == None):
        print "Cannot execute " + gpgCommand
        rat = enterGPGCommand()
    return gpgCommand

def getRatCommand():
    '''Gets an acceptable value for the Rat command'''
    if len(argv) == 1:
        ratCommand = enterRatCommand() 
    else:
        ratCommand = enterRatCommand(argv[1])
    return ratCommand

def getBaseDirectory():
    '''Gets an acceptable value for the base directory'''
    if len(argv) < 3:
        base = enterBaseDirectory()
    else:
        base = enterBaseDirectory(argv[2])    
    return base
    
def getGPGCommand():
    '''Gets an acceptable value for the command used to test signatures'''
    if len(argv) < 5:
        gpgCommand = enterGPGCommand('gpg')
    else:
        gpgCommand = enterGPGCommand(argv[4])
    return gpgCommand

def getBaseReportDirectory():
    if len(argv) < 4:
        return None
    else:
        return argv[3]

def checkArgs(f=lambda a,b,c,d: walkDirectories(a, b, c, d)):
    '''Prompts user for arguments missing from the command line. Check argument values.'''
    try:
        ratCommand = getRatCommand()
        base = getBaseDirectory()
        gpgCommand = getGPGCommand()
        baseReportDirectory = getBaseReportDirectory()
        
        f(base, ratCommand, gpgCommand, baseReportDirectory)
    except UserAbortError, e:
        print '\n'
        print e
    
checkArgs()
