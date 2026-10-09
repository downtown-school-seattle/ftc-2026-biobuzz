# FTC 26-27 BioBuzz

this is hendrix's branch of the code for the Downtown School's 2026-2027 robotics season

## teamcode

hi, this is my teamcode, if anything doesn't make sense to you, please talk to me, I have coded 99% of this and if you don't like it then please tell me instead of changing it without telling

## making a test opmode

if you are testing an opmode, copy an existing opmode and rename it (eg. RedAutoTest), after testing, if it is good, push it, this is to keep consistency

## autonomous explanation

there are 3 commands that work in autonomous as well as a way to group them, they are: drive(), turn(), motor()
there is also run() that allows you to run multiple commands at a time however, they need to be written as "[command]Cmd"

examples:
drive(90 [degrees], 12 [inches])
turn(45 [degrees])
motor(motor0e [motor name], 4200 [rpm])

run(driveCmd(180, 18), turnCmd(30), motorCmd(motor0e, 4000))

## motor naming

all the motors are named after their location on the control hub, the motors on the main hub are named motor# and on the expansion hub they are motor#e

## Contributors

- Hendrix C.
- Kieran B.