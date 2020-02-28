[toc]

# Java 接口回调理解以及网络请求



## 1、接口回调

### 1） 什么是接口回调？接口回调又是什么？

接口回调： 可以把使用某一接口的类创建的对象的引用赋给该接口声明的接口变量，那么该接口变量就可以调用被类实现的接口的方法。 

这句话是不是感觉很晦涩，反正我是这样认为。

咬文嚼字： 当接口变量**调用被类实现的接口中的方法**时，就是**通知相应的对象**调用接口的方法，这一**==过程==**称为对象功能的接口回调。 

其实还是很模糊，还是得配合一些例子来理解：

#### 案例一：

> 现在是疫情期间，所有学校的学生都得在家听老师上网课，
>
> 网课开始的时候老师会问：”同学们，你们能听见吗？能听见的在弹幕上扣1。“
>
> 然后同学们就疯狂打Call："1111111"。
>
> 为了让同学们知道老师看了他们扣的“1”，老师就会说“好的，我们开始上课。”反之，就会一直的问：“同学们听得见吗？”

这个例子里，**老师看见后说“好的，我们开始上课”**可以理解成回调这个过程。

##### 接口

```java
public interface CallBack {
    void response();
}
```

##### 老师类、学生类、Test

```java
public class Test {
    public static void main(String[] args) {
    	Teacher teacher = new Teacher();
    	Student students = new Student();
    	teacher.ask(students);
    }
}

class Teacher implements CallBack {
    public void ask(Student students) {
        System.out.println("你们能听见吗?能听见的扣1");
        students.call(this);        
    }
    
    @override
    public void response() {
        System.out.println("好的，我们开始上课。")；
    }
}

class Student {
    public void call(CallBack callBack) {
        System.out.println("111111111");
        callBack.response();
    }
}
```

##### 运行结果

> 你们能听见吗？能听见的扣1
>
> 111111111
> 好的，我们开始上课。
>
> 
>
> Process finished with exit code 0

##### 理解

这个例子里，老师实现了一个``CallBack``的接口和其中的方法``response``，然后从同学们扣“111111”开始，到老师说“好的，我们开始上课。”这一个过程就是接口``CallBack``的回调。



----



## 2、网络请求与回调

### 1）关系

在Android开发的过程中，常常需要使用到网络请求来获取数据，但是网络请求是个非常耗时的过程，就该放在子线程里，但是多线程的情况下，主线程不会主动等待子线程结束，在子线程结束时，子线程也不会主动通知主线程“我已经获取到了数据”，这样一来，网络请求后不能及时刷新就是问题。

或许会想到让主线程sleep一下，但是网络请求的时间是不好去估计。

sleep的时间太长，子线程已经请求完了，主线程还在sleep，那用户体验就不好；

sleep的时间太短，子线程还未请求完，主线程就已经sleep结束，那问题依旧存在。

很显然选择用主线程sleep这种方法来达到及时刷新是很烂很烂的做法；

因此我们为了完美解决这一问题，就需要使用到回调。我们在主线程将回调实例化，它能在网路请求结束时， 带着数据返回到主线程。

此时就等达到子线程成功获取到数据，紧接着主线程对数据进行处理，例如及时的更新UI。

### 2）具体代码

#### 回调接口

```java
public interface CallBack {

    void onResponse(String response);

    void onFailed(Exception e);
}
```

#### 网络请求类

```java
public class NetUitl {
    public void execute(Requset request, CallBack callBack) {
        try {
        // 具体的网路请求步骤
        String response = /*获取到的数据*/
            if (callBack != null) {
                callBack.onResponse(response);
            }
        } catch (IOException e) {
            callBack.onFailed(e);
        }   
    }
}
```

#### 使用网络请求的地方

```java
//.....
Request request = /* 建造 */
//.....省略新开线程的代码
@Override
public void run() {
    NetUitl.execute(request, new Callback(){
        @Override
        public void onResponse(String response) {
            //这里是拿到数据后的逻辑
        }
        
        @Override
        public void onFailed(Exception e) {
            //异常抛出
            e.printStackTrace();
        }
    });
}
//......

```

### 3）小结

我们封装了一个网络请求的工具类，这样一来，当我们使用到网络请求时，只需要将其实例化，再使用匿名内部类起到回调的作用。



----



## 3、源码

具体的网络请求工具类源码：[https://github.com/Seven749/NetUtil.git](https://github.com/Seven749/NetUtil.git)