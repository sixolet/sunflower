local PHI = 0.6180339887499

engine.name = "Sunflower"

local SCALE = {
    1,                         -- A
    1 + PHI^6,                 -- A#
    1 + PHI^5,                 -- B
    1 + PHI^4,                 -- C
    1 + PHI^4 + PHI^6,         -- C#
    1 + PHI^3,                 -- D
    1 + PHI^3 + PHI^5,         -- D#
    1 + PHI^2,                 -- E
    1 + PHI^2 + PHI^6,         -- F
    1 + PHI^2 + PHI^5,         -- F#
    1 + PHI^2 + PHI^4,         -- G
    1 + PHI^2 + PHI^4 + PHI^6, -- G#
}

brightnesses = {}

function midi_to_ratio(note)
    local octave = math.floor((note - 57)/12)
    local octave_ratio = (1/PHI)^octave
    local degree = (note - 57)%12 + 1
    return octave_ratio*SCALE[degree]
end

function process_midi(data)
    local d = midi.to_msg(data)
    if d.type == "note_on" then
        brightnesses[d.note] = brightnesses[d.note] + 10
        local ratio = midi_to_ratio(d.note)
        local velocity = d.vel/127
        local pitch = ratio*params:get("root")
        local amp = velocity^2 * params:get("amp")
        local release = params:get("release")
        local cutoff = params:get("cutoff")
        local filterEnv = params:get("filterEnv")
        engine.perc(pitch, amp, release, cutoff, filterEnv)
    end
end

function init()

    for i=1,127 do
        brightnesses[i] = 0
    end

    local midi_device = {} -- container for connected midi devices
    local midi_device_names = {}
    local target = 1

    local function midi_target(x)
        print("target!!!")
        midi_device[target].event = nil
        target = x
        midi_device[target].event = process_midi
    end

    for i = 1,#midi.vports do -- query all ports
        midi_device[i] = midi.connect(i) -- connect each device
        table.insert(midi_device_names,"port "..i..": "..util.trim_string_to_width(midi_device[i].name,40)) -- register its name
    end
    params:add_option("midi target", "midi target",midi_device_names,1,false)
    params:set_action("midi target", midi_target)  
    params:add_control("root", "root", controlspec.new(110, 440, 'exp', 0, 220, 'Hz', 0.001))
    params:add_control("amp", "amp", controlspec.new(0, 1, 'lin', 0, 0.3))
    params:add_control("release", "release", controlspec.new(0.01, 10, 'exp', 0, 0.5, 's'))
    params:add_control("cutoff", "cutoff", controlspec.FREQ)
    params:add_control("filterEnv", "filter env mod", controlspec.new(0, 10, 'lin', 0, 4))

    clock.run(function()
        while true do
            clock.sleep(1/15)
            redraw()
        end
    end)
    params:bang()
end

function redraw()
    screen.clear()
    screen.aa(1)
    screen.translate(64, 32)
    local decay = 1 - (1/(10*params:get("release")))
    if decay < 0 then decay = 0 end
    local angle = 2*math.pi*(clock.get_beats()%64)/64
    for radius=1,127 do
        local b
        if brightnesses[radius] > 1 then
            b = brightnesses[radius]
            brightnesses[radius] = b*decay
            b = math.floor(b)
        else
            b = 0
        end
        screen.level(4+b)
        screen.rotate(angle)
        screen.circle(radius/4.2, 0, 1+radius/60)
        screen.fill()
        screen.rotate(-angle)
        angle = (angle + (2*math.pi*PHI)) % (2*math.pi)
    end
    screen.translate(-64, -32)
    screen.update()
end