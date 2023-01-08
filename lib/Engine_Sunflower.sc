Engine_Sunflower : CroneEngine {

    classvar phi;

    var eoc, bus;

    *new { arg context, doneCallback;
		^super.new(context, doneCallback);
	}

    *initClass {
        phi = (sqrt(5) + 1)/2;
    }

    alloc {
        bus = Bus.audio(context.server, 2);
        eoc = {
            Out.ar(context.out_b, In.ar(bus, 2).tanh);
        }.play;
        SynthDef(\phisynth, { |out, freq=220, amp=0.5, releaseTime=2, cutoff=500, filterEnv=6|
    	    var numbers = 8.collect(_.value);
	        var partials = phi**numbers;
	        var env = Env.perc(releaseTime: releaseTime);
	        var eg = EnvGen.kr(env, doneAction: Done.freeSelf);
	        Out.ar(out, 0.3*amp*eg*RLPF.ar(Mix.ar(SinOsc.ar(freq*partials)/partials), cutoff * (1 + (eg*filterEnv)), 0.5)!2)
        }).add;

        SynthDef(\phifm, { |out, freq=220, amp=0.5, releaseTime=2, power=1, index=1, indexEnv=1|
	        var env = Env.perc(releaseTime: releaseTime);
	        var eg = EnvGen.kr(env, doneAction: Done.freeSelf);
            var modulator = SinOsc.ar(freq*(phi**power));
            var carrier = SinOsc.ar(freq, (pi*(index + (indexEnv*eg))*modulator).mod(2*pi));
            var snd = (0.3*amp*eg*carrier)!2;
            Out.ar(out, snd);
        }).add;        

        this.addCommand("perc", "fffff", { |msg|
            var freq = msg[1].asFloat;
            var amp = msg[2].asFloat;
            var releaseTime = msg[3].asFloat;
            var cutoff = msg[4].asFloat;
            var filterEnv = msg[5].asFloat;
            Synth(\phisynth, [
                \out, bus, 
                \freq, freq,
                \amp, amp,
                \releaseTime, releaseTime,
                \cutoff, cutoff,
                \filterEnv, filterEnv]);
        });

        this.addCommand("fmperc", "ffffff", { |msg|
            var freq = msg[1].asFloat;
            var amp = msg[2].asFloat;
            var releaseTime = msg[3].asFloat;
            var power = msg[4].asFloat;
            var index = msg[5].asFloat;
            var indexEnv = msg[6].asFloat;
            Synth(\phifm, [
                \out, bus, 
                \freq, freq,
                \amp, amp,
                \releaseTime, releaseTime,
                \power, power,
                \index, index,
                \indexEnv, indexEnv]);
        });
    }

    free {
        eoc.free;
        bus.free;
    }

}