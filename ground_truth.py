#!/usr/bin/env python
#
# Ground Truth ZeroMQ BlockCraft client
# By Zach "theY4Kman" Kanzler, 2011
#

from blockplayer import config
import numpy as np
import zmq
import sys
import os.path
import time

def startClient(filename):
  voxels = np.zeros([config.bounds[1][i]-config.bounds[0][i] for i in range(3)])>0
  with open(filename, "r") as fp:
    for y,layer in enumerate(eval(fp.read())):
      for z,row in enumerate(layer):
        for x,char in enumerate(row):
          voxels[x][8-y][z] = (char == '*')
  socket = zmq.Context().socket(zmq.PAIR)
  socket.connect('tcp://*:8134')
  while time.sleep(0.1) is None:
    socket.send(voxels)

if __name__ == "__main__":
  if len(sys.argv) != 2:
    print "Usage: %s path/to/ground/truth" % os.path.basename(sys.argv[0])
    sys.exit(2)
  
  startClient(sys.argv[1])

