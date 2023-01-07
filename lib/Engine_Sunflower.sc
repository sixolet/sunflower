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
    }

    free {
        eoc.free;
        bus.free;
    }

}