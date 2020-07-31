//com.mhook.sample
Java.perform(function () {
    const MainActivity = Java.use(
        "com.mhook.sample.task.MainActivity");
    MainActivity.test.overload()
        .implementation = function () {
        console.log("Main.test:" + this.test())
        return 54321;
    }
});
