![logo](http://i.imgur.com/unmUh3r.jpg)
# aDose

*This code is very old! It hasn't been touched in many years, and was never intented to be public in the first place.. proceed at your own risk!*

aDose is a "Brain Machine" app for Android.

Lay the phone above your eyes (open or closed), put in some headphones, choose a routine, and relax. You'll notice
your mind start to fall into some strange patterns. It actually works quite well, and I'm not even the kind of guy
who is into that hippy mumbo jumbo. Try it out! What's the worst that can happen? I guess you could go blind. And get tinnitus.

## Patterns

Patterns are defined in ".drugs" files, which are CSV representations of colors, sounds, and temporal patterns.

Ex:

    # lfreq, loffcolor, loncolor, rfreq, roff, ron, duration
    # delta = 402
    # theta = 406
    # alpha = 411
    # beta  = 415

    #delta
    400,ffffff,000000,402,000000,ffffff,60000
    #beta
    400,ffffff,000000,415,000000,ffffff,60000
    #alpha
    400,641,ffffff,000000,411,000000,ffffff,10000
    #theta
    400,641,ffffff,000000,406,000000,ffffff,10000

GPL 2014.
