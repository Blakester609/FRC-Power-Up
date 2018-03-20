# FRC-Power-Up
- Code for FRC Power Up robot
- This code is written using the WPILibrary for use on a roboRIO

### Things it does:

1. Has claw motors to bring the cube in and out
2. Has a motor to lift a telescoping arm to lift cubes to the switch and the scale
	- Uses an encoder to get absolute position of the lift motor and keep it from reversing direction, also keeps the cube lifted up when above 0.
3. Has multiple autonomous codes for putting cube on switch or scale
   - Can use Limelight for autonomous for the switch
