//com.mhook.sample
Java.perform(function () {
<<<<<<< HEAD
    const MainActivity = Java.use(
        "com.mhook.sample.task.MainActivity");
    MainActivity.test.overload()
        .implementation = function () {
        console.log("Main.test:" + this.test())
        return 54321;
    }
});
=======
    const MainActivity = Java.use("com.mhook.sample.MainActivity");
    MainActivity.test.overload().implementation = function () {
        return 54321;
    }
});
>>>>>>> 68eed3e42872565ebfb459cc70abcffacdf5d7f8
