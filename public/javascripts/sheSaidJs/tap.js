(function(e, t) {
    function n(e, t) {
        return (h ? t.originalEvent.touches[0] : t)["page" + e.toUpperCase()]
    }
    function a(t, n, a) {
        var r = e.Event(n, b);
        e.event.trigger(r, {
            originalEvent: t
        },
        t.target),
        r.isDefaultPrevented() && t.preventDefault(),
        a && (e.event.remove(D, x + "." + y, o), e.event.remove(D, p + "." + y, i))
    }
    function r(t) {
        var r = t.timeStamp || +new Date;
        l != r && (l = r, T.x = b.x = n("x", t), T.y = b.y = n("y", t), T.time = r, T.target = t.target, b.orientation = null, b.end = !1, d = !1, u = !1, v = setTimeout(function() {
            u = !0,
            a(t, "press")
        },
        e.Finger.pressDuration), e.event.add(D, x + "." + y, o), e.event.add(D, p + "." + y, i), w.preventDefault && t.preventDefault())
    }
    function o(t) {
        return b.x = n("x", t),
        b.y = n("y", t),
        b.dx = b.x - T.x,
        b.dy = b.y - T.y,
        b.adx = Math.abs(b.dx),
        b.ady = Math.abs(b.dy),
        (d = b.adx > w.motionThreshold || b.ady > w.motionThreshold) ? (clearTimeout(v), b.orientation || (b.adx > b.ady ? (b.orientation = "horizontal", b.direction = b.dx > 0 ? 1 : -1) : (b.orientation = "vertical", b.direction = b.dy > 0 ? 1 : -1)), t.target !== T.target ? (t.target = T.target, i.call(this, e.Event(p + "." + y, t)), void 0) : (a(t, "drag"), void 0)) : void 0
    }
    function i(e) {
        var t, n = e.timeStamp || +new Date,
        r = n - T.time;
        if (clearTimeout(v), e.target === T.target) {
            if (d || u) w.flickDuration > r && a(e, "flick"),
            b.end = !0,
            t = "drag";
            else {
                var o = c === e.target && w.doubleTapInterval > n - s;
                t = o ? "doubletap": "tap",
                c = o ? null: T.target,
                s = n
            }
            a(e, t, !0)
        }
    }
    var d, u, l, v, c, s, g = /chrome/i.exec(t),
    m = /android/i.exec(t),
    h = "ontouchstart" in window && !(g && !m),
    f = h ? "touchstart": "mousedown",
    p = h ? "touchend touchcancel": "mouseup mouseleave",
    x = h ? "touchmove": "mousemove",
    y = "finger",
    D = e("html")[0],
    T = {},
    b = {},
    w = e.Finger = {
        pressDuration: 300,
        doubleTapInterval: 300,
        flickDuration: 150,
        motionThreshold: 5
    };
    e.event.add(D, f + "." + y, r)
})(jQuery, navigator.userAgent);