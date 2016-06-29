rightMotorForward = 5
leftMotorForward = 8

rightMotorReverse = 6
leftMotorReverse = 7

gpio.mode(leftMotorForward, gpio.OUTPUT)
gpio.mode(rightMotorForward, gpio.OUTPUT)
gpio.mode(leftMotorReverse, gpio.OUTPUT)
gpio.mode(rightMotorReverse, gpio.OUTPUT)

gpio.write(rightMotorForward, gpio.LOW)
gpio.write(leftMotorForward, gpio.LOW)
gpio.write(rightMotorReverse, gpio.LOW)
gpio.write(leftMotorReverse, gpio.LOW)