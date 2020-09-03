# 标贝复刻技术SDK及Demo

# 1.Android Studio集成jar（参考demo）
## 1.1 将jar包添加至工程主module下，lib文件夹里。同步运行一下grale文件，加载该jar包。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020042415015110.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hzaHVhaWp1bjU1,size_16,color_FFFFFF,t_70)

## 1.2 在主module的build.gradle文件里，添加以下代码。
```java
dependencies {
	implementation 'com.squareup.okhttp3:okhttp:4.2.2'
        implementation 'com.google.code.gson:gson:2.8.6'
	implementation 'com.kailashdabhi:om-recorder:1.1.5'
}
```
## 1.3 在主Module的AndroidManifest.xml文件中添加网络权限。安卓6.0及以上系统版本在使用声音复刻SDK的时候，必须要申请RECORD_AUDIO和WRITE_EXTERNAL_STORAGE权限。
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
## 1.4 在主Module的AndroidManifest.xml文件中的application节点添加以下属性。
```java
android:usesCleartextTraffic="true"
android:requestLegacyExternalStorage="true"
```
**Eclipse环境也遵循相关集成jar包的方式即可。**

## 1.5 关于混淆
SDK中用到了okhttp和gson，所以需要将这两个包的混淆代码添加上。具体混淆代码可以去官方文档上查阅。如果项目中已经有这两个包的混淆代码，不必重复添加。请加上我们SDK其他类的混淆代码，如下：
```java
-keep class com.baker.synthesis.offline.bean.** { *; }
-keep public class com.baker.synthesis.offline.BakerConstants{*;}
-keep public class com.baker.synthesis.offline.authorization.AuthorizationCallback{*;}
-keep public class com.baker.synthesis.offline.OffLineSynthesizerEngine{*;}
-keep public class com.baker.synthesis.offline.OfflineBakerSynthesizer{*;}
-keep public class com.baker.synthesis.offline.BakerCallback{*;}
-keep public class com.baker.synthesis.offline.BakerMediaCallback{*;}
-keep public class com.baker.synthesis.offline.SynthesizerCallback{*;}
-keep public class com.baker.synthesis.offline.BaseMediaCallback{*;}
```

# 2.背景及核心流程介绍
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200424150250628.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hzaHVhaWp1bjU1,size_16,color_FFFFFF,t_70)
**2.1** 整个复刻体验闭环包含两个模块，一个模块是录音上传服务器进行声音模型训练，产出声音模型ID，另一个模块是根据声音模型ID去合成服务器合成，产出声音文件，播放声音文件完成体验。
**2.2** 此（复刻）SDK仅支持第一个模块的功能，即②③④⑤⑥⑦等功能。第二个体验的模块⑧，我们提供2种集成方式供选择，以便实现实际项目中的需求。
+ 第一种集成方式如Demo中所示，用声音模型ID + RestAPI的形式，合成MP3声音文件，进行播放。这种合成方式适用于单次不超过250字文本长度的文本合成。
+ 另一种方式则是声音模型ID + TTS合成SDK，具体集成方式可参考我们TTS合成SDK的接入文档。这种方式无文本长度限制，实时合成流式返回，TTS合成SDK中也有播放器功能，集成使用很方便。
# 3.SDK关键类
**3.1** BakerVoiceEngraver：声音复刻SDK关键业务处理类，SDK中单例存在。可通过BakerVoiceEngraver.getInstance()获得该实例。  
**3.2** DetectCallback：环境噪音检测回调类。开启环境检测后，检测结果会实时通过回调方法返回，最终结果以及错误信息都会通过此类的回调方法返回。  
**3.3** ContentTextCallback：录音文本内容信息获取回调类。文本内容以及错误信息会通过回调方法返回。  
**3.4** RecordCallback：录音及录音文件上传相关业务回调类。录音状态、文件上传识别、识别结果、错误信息等都会通过回调方法返回。录音过程中会实时将声音分贝值返回。   
**3.5** UploadRecordsCallback：录音完成后开启声音模型训练的回调类。提交结果、声音模型ID、错误信息等都会通过回调方法返回。  
**3.6** MouldCallback：查询声音模型信息回调类。可以通过模型ID查询单个模型的训练信息，可以通过queryId分页查询该Id下所有模型的训练信息。  
**3.7** Mould：声音模型的实体类。属性如下：
>+ String modelId; //模型ID
>+ int modelStatus;//模型状态 1=默认状态，2=录制中，3=启动训练失败，4=训练中，5=训练失败，6=训练成功。
>+ String statusName;//模型状态中文(值)
# 4.调用说明

### 4.1 本SDK调用过程可能会发生在多个页面，所以我们将各功能实现拆细了，同时伴随着细分的多个回调。这样做的目的是方便灵活调用。

### 4.2 第一步需要初始化。
可通过调用**BakerVoiceEngraver.getInstance().initSDK(Context context, String clientId, String clientSecret, String queryID)**实现初始化。前三个参数是必传参数，**clientId**和**clientSecret**是授权信息。第四个参数**queryID**可传空。**这个queryID的作用是与当前训练的声音模型ID关联，存储备份在标贝服务器。以备后期可通过queryId查询到与此管理的所有声音模型信息**。

如果在初始化时未上传queryId，也可以调用**BakerVoiceEngraver.getInstance().setQueryId(String queryID)**方法设置queryID，但设置queryID一定要在调用**getVoiceMouldId()**方法之前调用，即4.4步之前设置。在产出声音模型ID时，就需要将queryId与声音模型ID关联存储。

### 4.3 环境噪音检测。
在此步之前，请务必申请到**Manifest.permission.RECORD_AUDIO**, **Manifest.permission.WRITE_EXTERNAL_STORAGE** 这2个权限。SDK中也做了权限检测，无权限检测会上报错误信息。

**BakerVoiceEngraver.getInstance().setDetectCallback(DetectCallback callback)**;设置噪音检测的回调。检测过程中会将实时分贝数据、检测最终结果、错误信息等通过回调接口中的方法返回。DetectCallback接口的具体信息请参考**5.2**。

调用**BakerVoiceEngraver.getInstance().startDBDetection()**;方法开启噪音检测。检测时长是3秒。

噪音检测通过固定算法得出环境声音分贝值作为检测终值，如果终值大于70分贝，是不允许进行后续步骤的，因为环境太嘈杂，会直接影响训练出来的声音的品质。可以参考demo中对于返回结果的提示以及逻辑处理，如果噪音检测不通过，可以换环境在相对安静的空间内重新检测。重新检测也是调用**BakerVoiceEngraver.getInstance().startDBDetection()**;方法。

理论上是环境越安静，录音效果越好，训练出来的声音品质会越好。所以对这个噪音的限制，SDK只做不能超过70分贝的最大值限制。使用SDK的亲们可以在自己的应用中灵活做二次限制。

### 4.4 获取录音文本，并录音上传。
在此步也请一定确保申请到**Manifest.permission.RECORD_AUDIO**, **Manifest.permission.WRITE_EXTERNAL_STORAGE** 这2个权限。SDK中也做了权限检测，无权限检测会上报错误信息。

**BakerVoiceEngraver.getInstance().setContentTextCallback(ContentTextCallback callback)**;设置获取录音文本的回调。文本信息或错误信息都将通过回调方法返回。

**BakerVoiceEngraver.getInstance().setRecordCallback(RecordCallback callback)**;设置录音、上传、识别检测、错误信息等接口方法的回调。

**BakerVoiceEngraver.getInstance().getTextList()**;调用此方法获取录音文本信息。参考demo，将返回的文本信息列表分条展示，供用户完成录音流程。

**BakerVoiceEngraver.getInstance().getVoiceMouldId()**;调用此方法申请此次声音训练的相关资源。在开始录音上传之前，必须调用此方法。

**BakerVoiceEngraver.getInstance().startRecord(String contentText)**;调用此方法开启录音。此方法会返回一个int值，该值的意义分别是：**0=mouldId为空，1=无权限，2=开启成功**。

**BakerVoiceEngraver.getInstance().uploadRecords(String contentText)**;本小段朗读完成后，调用此方法停止录音，并开始上传识别。此方法会返回一个int值，该值的意义分别是：**0=mouldId为空,  1=结束成功，开始上传识别。**

若结束录音后上传过程中出现错误，可以通过调用以下方法**BakerVoiceEngraver.getInstance().reUploadRecords(String contentText)**;重新上传本段录音。

若因各种原因，在录音过程中需要退出录制，或者放弃当前任务，请调用**BakerVoiceEngraver.getInstance().recordInterrupt()**;这个方法通知SDK中止录音。若在异常退出录制的过程中调用此方法，**可以即时释放声音复刻名额以及停止录音**。否则未中止录音可能会产生非常大的录音文件，占用用户设备存储资源。

此步中我们将录音的顺序及录音总数的维护交给了SDK接入方来维护。理论上应当一段一段录制，当检测到都录制完成后，就开启声音模型训练了。

### 4.5 开启声音模型训练。
**BakerVoiceEngraver.getInstance().setUploadRecordsCallback(UploadRecordsCallback callback)**;通过此方法设置开启模型训练结果的回调。开启训练的结果、声音模型ID、错误信息等都将通过此回调接口方法返回。

**BakerVoiceEngraver.getInstance().finishRecords(String phone, String notifyUrl)**;调用此方法开启模型训练。这个方法中的两个参数都是选填的。
+ 第一个参数是手机号，如果上传后，可以通过手机短信收到训练进度通知，但短信中带有标贝公司签名信息，所以**不建议在正式项目中上传该手机号**。
+ 第二个参数是提供一个接收异步训练进度回调的服务器接口地址，**建议SDK接入方设置该回调地址**。标贝科技的异步训练服务器将训练结果以推送方式通知接入方，该地址必须为外网可访问的url，不能携带参数。（文档最后附了示例回调接口的代码，提供参考）目前主要包括开启训练通知和训练完成时通知。

开启模型训练成功后，此步回调方法中返回的mouldId建议SDK接入方自行将其存储维护起来，此mouldId应当与用户一一对应。在声音合成体验时，要用到这个mouldId。

### 4.6 如果需要再次发起新声音模型训练，可以重复第4.3-4.5步。

### 4.7 查询模型信息。
**BakerVoiceEngraver.getInstance().setMouldCallback(MouldCallback callback)** 设置查询结果回调。声音模型相关信息、错误信息等会通过回调方法返回。

**BakerVoiceEngraver.getInstance().getMouldList(int page, int limit, String queryId)** 根据queryId分页查询此queryId下的所有声音模型信息。Page从1开始。

**BakerVoiceEngraver.getInstance().getMouldInfo(String mouldId)**;根据模型Id查询模型信息。

### 4.8 体验合成声音。
+ 第一种方式类似Demo所示，通过restAPI形式，拼接MP3链接，然后请求播放。这个地方需要token信息，这个token信息可以通过**BakerVoiceEngraver.getInstance().getToken()**;实时获取。Token其实是用sdk初始化时提供的clientId和clientSecret获取的，且有过期时效，所以不建议保存，使用时实时获取即可。相关介绍可以参考标贝公司官网：[https://www.data-baker.com/tts_api_rest.html](https://www.data-baker.com/tts_api_rest.html)
+ 第二种方式是集成声音合成的SDK，这种方式接入的相关链接：[https://www.data-baker.com/tts_api_androidsdk.html](https://www.data-baker.com/tts_api_androidsdk.html)) SDK采用实时合成流式返回，响应速度很快。SDK也集成了播放器功能，集成使用很方便。

# 5.各类API明细
## 5.1BakerVoiceEngraver类说明
方法名     | 作用       | 说明    
---------------------- | -------------------------- | -------------------------------------
getInstance()  | 	获取单实例  | 	获取BakerVoiceEngraver类单实例。
initSDK()	| 	初始化SDK	| 	initSDK(Context context, String clientId, String clientSecret, String queryID)初始化SDK。前三个参数是必传参数，clientId和clientSecret是授权信息。第四个参数queryID可传空。这个queryID的作用是与当前训练的声音模型ID关联，存储备份在标贝服务器。以备后期可通过queryId查询到与此管理的所有声音模型信息。
setQueryId()	| 	设置queryId		| 如果在初始化时未上传queryId，也可以调用setQueryId(String queryID)方法设置queryID，但设置queryID一定要在调用getVoiceMouldId()方法之前调用，即4.4步之前设置。
getTextList()	| 	获取录音文本信息	| 	调用此方法获取录音文本信息。参考demo，将返回的文本信息列表分条展示，供用户完成录音流程。
startDBDetection()	| 	开启噪音检测	| 	噪音检测通过固定算法得出环境声音分贝值作为检测终值，如果终值大于70分贝，是不允许进行后续步骤的，因为环境太嘈杂，会直接影响训练出来的声音的品质。可以参考demo中对于返回结果的提示以及逻辑处理，如果噪音检测不通过，可以换环境在相对安静的空间内重新检测。重新检测也是调用此方法。
getVoiceMouldId()	| 	申请声音模型训练资源	| 	调用此方法申请此次声音训练的相关资源。在录音上传之前必须调用此方法。
startRecord()	| 	开始录音		| 调用startRecord(String contentText)方法开启录音。参数中contentText是此条录音的文本信息（必传）。此方法会返回一个int值，该值的意义分别是：0=mouldId为空，1=无权限，2=开启成功。
uploadRecords()		| 停止录音上传识别	| 	每小段朗读完成后，调用uploadRecords(String contentText)方法停止录音，并开始上传识别。参数中contentText是此条录音的文本信息（必传）。此方法会返回一个int值，该值的意义分别是：0=mouldId为空,  1=结束成功，开始上传识别。
reUploadRecords()	| 	重新上传本段录音	| 	若结束单段录音后上传过程中出现错误，可以通过调用以下方法reUploadRecords(String contentText);重新上传本段录音。参数中contentText是此条录音的文本信息（必传）。此方法会返回一个int值，该值的意义分别是：0=mouldId为空,  1=结束成功，开始上传识别。
recordInterrupt()		| 异常中断(退出)此次录音任务	| 	若因各种原因，在录音过程中需要退出录制，或者放弃当前任务，请调用recordInterrupt()方法通知SDK中止录音。若在异常退出录制的过程中调用此方法，可以即时释放声音复刻名额以及停止录音。否则未中止录音可能会产生非常大的录音文件，占用用户设备存储资源。
finishRecords()		| 完成录音开启训练	| 	调用此方法开启模型训练。这个方法中的两个参数都是选填的。第一个参数是手机号，如果上传后，可以通过手机短信收到训练进度通知，但短信中带有标贝公司签名信息，所以不建议在正式项目中上传该手机号。第二个参数是训练进度回调的服务器接口地址，建议SDK接入方设置该回调地址，这样我们服务器在模型训练过程中的进度变更时通知该地址，以便及时通知用户。目前主要包括开启训练通知和训练完成时通知。
getMouldList()		| 获取模型信息列表	| 	getMouldList(int page, int limit, String queryId)根据queryId分页查询此queryId下的所有声音模型信息。Page从1开始。
getMouldInfo		| 获取单个模型信息		| getMouldInfo(String mouldId);根据模型Id查询模型信息。
getToken()	| 	获取token信息	| 	getToken()实时获取Token。Token其实是用sdk初始化时提供的clientId和clientSecret获取的，且有过期时效，所以不建议保存，使用时实时获取即可。
setContentTextCallback()		| 设置获取录音文本的回调	| 	setContentTextCallback(ContentTextCallback callback)设置获取录音文本的回调。文本信息或错误信息都将通过回调方法返回。
setDetectCallback()		| 设置噪音检测的回调	| 	setDetectCallback(DetectCallback callback)设置噪音检测的回调。参数需要接入方自己实例化DetectCallback，可参考demo的方式。检测过程中会将实时分贝数据、检测最终结果、错误信息等通过回调接口中的方法返回。DetectCallback接口的具体信息请参考xxx。
setRecordCallback()	| 	设置录音上传相关回调	| 	setRecordCallback(RecordCallback callback)设置录音、上传、识别检测、错误信息等接口方法的回调。
setUploadRecordsCallback()	| 	设置开启模型训练结果的回调。		| setUploadRecordsCallback(UploadRecordsCallback callback)通过此方法设置开启模型训练结果的回调。开启训练的结果、声音模型ID、错误信息等都将通过此回调接口方法返回。
setMouldCallback()		| 设置查询声音模型信息的回调。	| 	setMouldCallback(MouldCallback callback)设置查询结果回调。声音模型相关信息、错误信息等会通过回调方法返回。
startPlay() | 播放录音 | 第一个参数是试听第几个录音，从 0 开始，第二个参数是个回调，详见 5.7。
stopPlay() | 停止播放 | 停止播放已经开始的录音。
isRecord(int index) | 判断是否录制成功 | index 从 0 开始。x 从 0 开始。如果录制成功返回 true。如果录制失败返回 false。



## 5.2 DetectCallback环境检测回调接口方法说明
接口方法名	     |   作用     | 说明
------ | --------------- | --------- 
dbDetecting | 	环境检测中结果反馈 | 	dbDetecting(int value);回调方法的参数是实时返回的声音分贝检测值。使用方式参考demo。
dbDetectionResult	 | 环境检测最终结果反馈 | 	dbDetectionResult(boolean result, int value);参数result=true，表示检测通过。value是检测通过的分贝值。result=false，表示检测未通过。value是检测未通过的分贝值。使用方式参考demo。
onDetectError | 	错误信息回调	 | onDetectError(int errorCode, String message);错误信息回调，errorCode是错误code码，message是详细错误信息。使用方式参考demo。

## 5.3 ContentTextCallback获取录音文本回调接口方法说明
接口方法名	     |   作用     | 说明
------ | --------------- | --------- 
contentTextList	 | 获取录音文本回调 | 	contentTextList(String[] strList);获取录音文本回调，返回的参数是此次录音需要的录音文本，以字符串数组形式返回。可参考demo维护和使用该数据。
onContentTextError | 	错误信息回调 | 	onContentTextError(int errorCode, String message);错误信息回调，errorCode是错误code码，message是详细错误信息。使用方式参考demo。
## 5.4 RecordCallback录音上传识别回调接口方法说明
接口方法名	     |   作用     | 说明
------ | --------------- | --------- 
recordsResult	 | 录音中、识别中、识别结果回调。 | 	recordsResult(int typeCode, int recognizeResult);录音中、识别中、识别结果回调。参数typeCode 1=录音中， 2=识别中，3=最终结果。recognizeResult是识别率，取值0-100. 使用方式参考demo，非常建议单段录音识别通过后才开启下一段录制。所有段录音完成录制，识别通过才开启模型训练，否则会影响最终声音的品质。
recordVolume	 | 录音过程中实时返回声音分贝信息。 | 	recordVolume(int volume);录音过程中，会将声音分贝值实时返回。volume是分贝值。具体使用方式参考demo。
onRecordError	 | 错误信息回调 | 	onRecordError(int errorCode, String message);错误信息回调，errorCode是错误code码，message是详细错误信息。使用方式参考demo。
## 5.5 UploadRecordsCallback开启模型训练回调接口方法说明
接口方法名	     |   作用     | 说明
------ | --------------- | --------- 
uploadRecordsResult | 	模型开启训练成功回调 | 	uploadRecordsResult(boolean result, String mouldId);参数result true=成功，false=失败。参数mouldId是声音模型的ID，此mouldId建议SDK接入方自行将其存储维护起来，此mouldId应当与用户一一对应。在声音合成体验时，要用到这个mouldId。 使用方式参考demo。
onUploadError | 	错误信息回调 | 	onUploadError(int errorCode, String message);错误信息回调，errorCode是错误code码，message是详细错误信息。使用方式参考demo。提交不成功，可以再次提交。
## 5.6 MouldCallback查询声音模型相关回调接口方法说明
接口方法名	     |   作用     | 说明
------ | --------------- | --------- 
mouldInfo | 	根据mouldId查询mould信息回调 | 	mouldInfo(Mould mould);根据mouldId查询mould信息回调，方法的参数是返回的声音模型信息。若需要此回调方法，需要手动复写该方法。使用方式参考demo。
mouldList | 	根据queryId分页查询mould信息回调 | 	mouldList(List<Mould> list);根据queryId分页查询mould信息回调。参数list是声音模型信息的列表。使用方式参考demo。
onMouldError | 	错误信息回调 | 	onMouldError(int errorCode, String message);错误信息回调，errorCode是错误code码，message是详细错误信息。使用方式参考demo。

## 5.7 PlayListener，录音试听回调
接口方法名	     |   作用     | 说明
------ | --------------- | ---------
playStart | 试听开始 | 当开始播放的时候回调此方法
playEnd | 试听结束 | 当播放结束的时候回调此方法
playError | 试听报错 | 当播放过程中出现错误的时候回调此方法时候回调此方法

# 6.失败时返回的code对应表
错误码 | 	含义
------ | ---------------
90000	 | 接口正常，正确返回的识别码
90001	 | 请求token失败
90002	 | Token过期
90003	 | 参数值为空或不正确
90004	 | 网络请求错误
90005	 | 网络请求返回data值为空
90006	 | 服务器返回错误的代码
90007	 | 解析response出错
90008	 | 当前上传的录音的mouldId与录制的录音的mouldId不一致
90009	 | 当前上传的录音的contentText与录制的录音的contentText不一致
90010	 | 停止录音出错
90011	 | 停止检测噪音出错
90012	 | 创建录音文件时异常
90013	 | 因音频焦点丢失或电话等异常中断录音
99999	 | 服务器系统异常
00011	 | Token校验失败，请核查！
10003	 | 参数错误
10004	 | 上传文件失败，请选择正确的文件格式
10005	 | 上传文件不能为空
10008	 | 模型id不合法，请重试
10009	 | 模型正在过程录制中，其他客户端不能同时录制！
10010	 | 识别语音超时！
40002	 | 提交次数已达到最大限制
40003	 | 接口请在有效期内使用
40004	 | 接口签名不合法
40005	 | 请填写正确的手机号！
# 7.异步回调接口方法代码示例

```java
@ApiOperation(value = "模型训练结果回调接口，仅作为示例", notes = "该链接通过参数notifyUrl设置，如果链接无法访问，将无法接收到回调的push信息。")
    @PostMapping("/notify")
    public void mouldInformationNotify(HttpServletRequest request, HttpServletResponse response) {
        LogUtils.printStartTag(request.getRequestURI());
        String resXml = "";
        InputStream inStream;
        try {
            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }

            // 获取 模型训练方 调用notifyUrl时携带的返回信息
            String result = new String(outSteam.toByteArray(), "utf-8");
            log.info("dataBaker:模型训练返回结果 ----result---- =" + result);
            // 关闭流
            outSteam.close();
            inStream.close();
            // String转换为json对象，然后业务处理
            JSONObject jsonObject = StringUtils.isNotEmpty(result) ? JSON.parseObject(result) : null;

            //该参数表示模型id
            String mouldId = jsonObject == null ? "" : jsonObject.getString("mouldId");
            //该参数表示模型当前状态：可能的值为0、1、2、3
            String mouldStatus = jsonObject == null ? "" : jsonObject.getString("mouldStatus");
            //该参数表示状态的说明（文案可能会有变动），0：训练任务已启动 1：训练任务启动失败 2：训练任务启动成功但训练失败 3：训练成功
            String statusName = jsonObject == null ? "" : jsonObject.getString("statusName");

            //具体业务处理，例如自行通知用户、存储模型id和状态信息等（可先返回结果，然后异步去完成业务逻辑）

            //todo

            //根据情况，向结果中赋值：正常情况下，选择下面的resSuccess;如果选择resFail,将会视为推送失败，标贝服务端将重新推送一次相同的内容
            //     * 返回成功xml
            //     */
            //    String resSuccess = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            //    /**
            //     * 返回失败xml
            //     */
            //    String resFail = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[报文为空]]></return_msg></xml>";

            resXml = Constant.resSuccess;
            //记录日志
            log.info("dataBaker:模型训练回调返回模型  {}  的状态为：--->{}", mouldId, statusName);
        } catch (Exception e) {
            log.error("dataBaker:模型训练回调异常：", e);
        } finally {
            try {
                // 处理业务完毕
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                log.error("dataBaker:模型训练回调异常:out：", e);
            }
        }
    }
```

