# Wadjet Android - سكريبت العرض (عامية مصرية)

> ده السكريبت اللي المقدم هيقوله بصوته وقت العرض. كل سلايد ليها فقرة.
> التيرمز التقنية بالإنجليزي، الشرح بالعربي.

---

## Slide 01 — Title

السلام عليكم ورحمة الله. إحنا فريق Wadjet، من كلية الذكاء الاصطناعي، جامعة كفر الشيخ. النهارده هنقدملكم مشروع التخرج بتاعنا: Wadjet.

Wadjet ده اسم الإلهة اللي بتمثل عين حورس، حارسة مصر القديمة. والأبلكيشن ده بيحمي التراث المصري القديم بطريقة تانية: بيخلي أي حد يقدر يقراه ويفهمه.

**Key points**:
- قدم الفريق بإيجاز
- خلي الاسم يعلق في دماغهم: Wadjet = حارسة التراث

**Transition**: طب ايه المشكلة اللي الأبلكيشن ده بيحلها؟

---

## Slide 02 — The problem

[POINT TO SCREEN]

تخيل إنك رايح معبد الكرنك. بتصور الجدران. بتطلع بـ 200 صورة وصفر فهم. ده اللي بيحصل مع 99.9% من الزوار.

المشكلة مش إن المعلومات مش موجودة. المشكلة إنها متفرقة. القاموس في كتاب أكاديمي بـ 400 دولار. الأساطير على 12 موقع مختلف. أدوات الكتابة في أبلكيشن تاني خالص.

[PAUSE]

مفيش حاجة واحدة تجمعلك كل ده.

**Key points**:
- 3 مشاكل: مش بنعرف نقرا، مفيش أداة واحدة، المعلومات متفرقة
- خلي الكلام شخصي، مش أكاديمي

**Transition**: فإحنا عملنا حاجة تجمع كل ده في مكان واحد.

---

## Slide 03 — The solution

[POINT TO SCREEN]

Wadjet فيه 6 أدوات رئيسية.

Scan: تصور هيروغليفي والأبلكيشن يعرفلك كل حرف. Dictionary: أكتر من 1,000 علامة Gardiner، أوفلاين. Write: تكتب كلمة بالإنجليزي يطلعلك هيروغليفي وتسمع النطق. Explore: أكتر من 260 موقع أثري مصري بصور وتاريخ. Thoth: شات بوت AI متخصص في المصريات، اسألوه أي حاجة. Stories: 12 أسطورة مصرية بالرسومات والسرد.

الأبلكيشن native Android، مبني بـ Kotlin و Jetpack Compose، وشغال بالعربي والإنجليزي.

**Key points**:
- اذكر كل feature بسرعة، متقعدش في واحدة
- ركز على الأرقام: 1,000+ علامة، 260+ موقع، 12 أسطورة

**Transition**: خلونا نشوف أهم feature بالتفصيل.

---

## Slide 04 — Scan & translate

[POINT TO SCREEN]

الـ scan ده أهم feature في الأبلكيشن. الفكرة بسيطة: صور، يكتشف، يصنف، يترجم. أربع خطوات.

الكاميرا بتستخدم CameraX، اللي هي الـ native camera API بتاعة Android. الصورة بتتحلل لـ individual glyphs، كل glyph بيتصنف من ضمن 171 كلاس Gardiner. الدقة 98.2%.

ومش model واحد. تلات models بيشتغلوا مع بعض: واحد بيكتشف مكان الهيروغليفي في الصورة، واحد بيصنف كل حرف، والتالت للمعالم السياحية.

الموضوع كله بياخد أقل من 5 ثواني.

[PAUSE]

**Key points**:
- Pipeline من 4 خطوات
- 98.2% دقة على 171 كلاس
- 3 models بيشتغلوا مع بعض
- أقل من 5 ثواني للعملية كلها

**Transition**: غير السكان، الأبلكيشن فيه قاموس وأداة كتابة.

---

## Slide 05 — Dictionary & write

[POINT TO SCREEN]

على الشمال: القاموس. فيه 1,023 علامة Gardiner كاملة، مقسمين لـ 26 فئة من A لحد Aa. تقدر تدور بالاسم أو الصوت أو الوصف. وكل ده أوفلاين، مخزن في Room database محلي.

على اليمين: أداة الكتابة. مش بتستبدل حرف بحرف زي الأبلكيشنات التانية. بتعمل word-level transliteration. يعني لو كتبت "Eternal life" يطلعلك الهيروغليفي الصح مع النطق: "ankh djet".

وفيه TTS بيحول الـ transliteration لنطق: الـ x بتتحول لـ kh، الـ S بتتحول لـ sh، وهكذا.

**Key points**:
- 1,023 علامة أوفلاين
- Word-level مش letter-level transliteration
- TTS للنطق

**Transition**: لسه فيه 3 features تانيين.

---

## Slide 06 — Explore, Thoth, stories

[POINT TO SCREEN]

Explore: أكتر من 260 موقع أثري. لو صورت معلم سياحي الموديل بيتعرف عليه بدقة 93.8% من ضمن 52 موقع. و 1,220 صورة curated من Wikimedia.

Thoth: شات بوت AI متخصص في المصريات. سميته على اسم إله المعرفة عند المصريين القدماء. بيفتكر السياق، تقدر تعدل الرسائل، ولو حصل error يحاول تاني.

Stories: 12 أسطورة مصرية. عين حورس، إيزيس وأوزوريس، رحلة رع. كل واحدة بالرسومات والسرد الصوتي وتتبع التقدم.

**Key points**:
- 3 features بسرعة، متقعدش في واحدة
- الأرقام: 260+ موقع، 93.8% دقة، 12 أسطورة

**Transition**: خلونا نوريكم الأبلكيشن شغال.

---

## Slide 07 — Live demo

[PICK UP PHONE]

خلونا نوريكم الأبلكيشن على الموبايل.

هنعمل 3 حاجات:
1. هنصور هيروغليفي والأبلكيشن يترجمه
2. هنسأل Thoth سؤال عن مصر القديمة
3. هنكتب اسم بالإنجليزي ونشوف الهيروغليفي بتاعه

[PAUSE — do the demo]

[WAIT FOR QUESTION]

**Key points**:
- حضر الديمو قبل العرض
- لو حاجة فشلت اشرح ليه (network، model loading time)
- متقلقش لو أخدت وقت

**Transition**: خلونا نرجع للعرض ونشوف الأبلكيشن من جوا.

---

## Slide 08 — Architecture

[POINT TO SCREEN]

الأبلكيشن مبني على 3 layers رئيسية.

الأندرويد app مكتوب بـ Kotlin و Jetpack Compose مع Hilt للـ dependency injection. بيكلم backend API مبني بـ FastAPI وموجود على Hugging Face Spaces. الـ ML models الـ 3 شغالين بـ ONNX Runtime على الموبايل نفسه.

الأبلكيشن مقسم لـ 18 module: 10 feature modules و 8 core modules.

الـ feature modules هي auth, chat, scan, dictionary, explore, feedback, landing, settings, stories, dashboard.

الـ core modules هي common, data, domain, network, database, designsystem, ml, ui, firebase.

الفكرة إن كل feature module يعتمد على الـ core بس مش يعرف حاجة عن أي feature تاني. Clean architecture.

**Key points**:
- 3 layers: Android, Backend, ML
- 18 module (10 feature + 8 core)
- Clean architecture: feature modules مش بتشوف بعض

**Transition**: ايه الـ tech stack اللي بنينا بيه ده؟

---

## Slide 09 — Tech stack

[POINT TO SCREEN]

ده الـ tech stack كامل. مش هنقرا كل حاجة، بس أهم النقط:

Kotlin 2.1 للـ language. Jetpack Compose مع Material 3 للـ UI، وده اللي عملنا بيه الـ Egyptian gold theme. Hilt للـ dependency injection لأنها compile-time وبتعمل scoping per module.

Room للداتابيز المحلي، فيه القاموس وتاريخ الشات. Retrofit مع kotlinx.serialization للـ API calls. CameraX للكاميرا. ONNX Runtime للـ ML على الموبايل. Firebase للـ auth والـ analytics.

الأبلكيشن بيستهدف API 35 والـ minimum هو API 26، يعني Android 8.0 فما فوق.

**Key points**:
- ركز على الـ "why" مش الـ "what"
- متقراش كل row

**Transition**: خلونا نشوف الـ ML بالتفصيل.

---

## Slide 10 — ML deep dive

[POINT TO SCREEN]

عندنا 3 models:

الأول: Hieroglyph Classifier. دقته 98.2% على 171 كلاس Gardiner. CNN مع transfer learning.

التاني: Landmark Identifier. دقته 93.8% على 52 موقع مصري. متدرب على صور curated من Wikimedia.

التالت: Glyph Detector. بيحدد مكان كل هيروغليفي في الصورة ويدينا bounding boxes. ده بيغذي الـ classifier.

الموديلات متدربة بـ TensorFlow و Keras، بعد كده بتتحول لصيغة ONNX، وبتشتغل على الموبايل بـ ONNX Runtime.

[PAUSE]

نقطة مهمة: الصور مش بتطلع من الموبايل. كل الـ inference بيحصل locally. مفيش server upload.

**Key points**:
- 3 models: classifier (98.2%), landmark (93.8%), detector
- Pipeline: TensorFlow → ONNX → Android
- الخصوصية: كل حاجة on-device

**Transition**: طبعا مش كل حاجة مشيت smooth. خلونا نحكيلكم ايه اللي وقع.

---

## Slide 11 — Challenges

[POINT TO SCREEN]

تلات مشاكل حقيقية وقعت وحليناها:

الأولى: token leak. الـ AuthInterceptor كان بيبعت الـ Bearer token مع كل request، حتى لو الـ request رايح لـ Wikipedia عشان يجيب صورة. يعني بنبعت الـ auth headers بتاعتنا لـ CDN خارجي. الحل: بقينا بنتشيك إن الـ URL بتاع الـ API base قبل ما نحط الـ token. الـ external requests بتعدي من غير auth.

[PAUSE]

التانية: crash في الشات. الـ message IDs كانت بتستخدم sessionId_timestamp. لو رسالتين اتبعتوا في نفس الـ millisecond، الـ IDs بتتكرر والأبلكيشن بيقع. الحل: ضفنا array index في الـ ID format: sessionId_index_timestamp.

التالتة: 18 modules بيعتمدوا على بعض. الـ feature modules محتاجة core بس مش المفروض تعرف حاجة عن بعض. الحل: الـ domain layer بتعرف الـ contracts، والـ navigation بـ type-safe Compose Nav 2.8+.

[WAIT FOR QUESTION]

**Key points**:
- 3 مشاكل حقيقية، مش theoretical
- الـ token leak ده security issue حقيقي
- الـ duplicate IDs ده production crash

**Transition**: خلونا نشوف الأرقام والمقارنة.

---

## Slide 12 — Competition & numbers

[POINT TO SCREEN]

في الجدول ده مقارنة بين Wadjet و Google Lens والأبلكيشنات التانية ويكيبيديا.

Google Lens بيعمل OCR عام، مش متخصص في هيروغليفي. الأبلكيشنات التانية عندها features محدودة. ويكيبيديا مفيش فيها أدوات تفاعلية.

Wadjet بيعمل كل حاجة: scan بدقة 98.2%، قاموس أوفلاين 1,023 علامة، كتابة بالهيروغليفي مع نطق، 260+ موقع أثري، شات AI، 12 أسطورة، عربي وإنجليزي.

الأرقام تحت:
- 1,023 علامة Gardiner
- 260+ موقع أثري
- 12 أسطورة
- 1,220+ صورة
- 98.2% دقة الهيروغليفي
- 93.8% دقة المعالم
- حجم الـ APK حوالي 88 ميجا

**Key points**:
- الجدول بيتكلم لوحده
- ركز على إن مفيش حد تاني بيعمل كل ده في حاجة واحدة

**Transition**: ايه اللي جاي؟

---

## Slide 13 — Future vision

[POINT TO SCREEN]

فيه 6 حاجات في الخطة الجاية:

الأولى: live camera scan. بدل ما تصور وتستنى، الكاميرا تشتغل real-time مع AR overlay.

التانية: deep translation. دلوقتي بنصنف كل حرف لوحده. الخطوة الجاية إننا نفهم الجمل كاملة، القواعد النحوية.

التالتة: offline ML. ننزل الموديلات جوا الـ APK نفسه عشان الـ scan يشتغل من غير نت خالص.

الرابعة: شراكات مع المتاحف. تصور المعروضات والأبلكيشن يديك القصة والسياق.

الخامسة: نزود الأساطير لـ 50+. سير الفراعنة، قصص الحياة اليومية.

السادسة: education mode. منهج للمدارس فيه دروس وكويزات وتتبع تقدم.

**Key points**:
- متقعدش في كل واحدة. اذكرها بسرعة
- ده forward-looking، مش وعود

**Transition**: شكرا ليكم.

---

## Slide 14 — Thank you

شكرا جزيلا ليكم على وقتكم.

الأبلكيشن متاح كـ APK والويب فيرجن شغالة على nadercr7-wadjet-v2.hf.space.

الكود على GitHub: github.com/Nadercr7.

لو عندكم أي أسئلة إحنا موجودين.

[WAIT FOR QUESTION]

---

## Q&A — أسئلة متوقعة

### 1. ليه مش استخدمت Google Lens؟

Google Lens بيعمل OCR عام، يعني بيقرا نصوص حديثة. الهيروغليفي مش نص عادي، ده 171 كلاس مختلف من علامات Gardiner. كل علامة ليها شكل مختلف تماما. Google Lens مش متدرب على ده. إحنا درّبنا model متخصص على الـ Gardiner Sign List وجبنا دقة 98.2%.

### 2. الداتا جت منين؟

الداتا من مصادر مختلفة. علامات الهيروغليفي من الـ Gardiner Sign List اللي هي المرجع الأكاديمي المعروف. صور المعالم curated من Wikimedia Commons. الأساطير مكتوبة من مصادر أكاديمية. القاموس فيه 1,023 علامة مأخوذة من الـ standard Gardiner classification.

### 3. ايه الموديل اللي استخدمته؟

CNN architecture مع transfer learning. درّبنا بـ TensorFlow و Keras وبعد كده حولنا لـ ONNX format. اخترنا ONNX عشان يشتغل على الموبايل بدون server. الـ ONNX Runtime بتاع Android خفيف وسريع.

### 4. ليه ONNX مش TFLite؟

سؤال حلو. TFLite كان خيار. بس ONNX Runtime ليه ميزة: بيشتغل على أي platform مش بس Android. لو قررت أعمل iOS version أو desktop app، نفس الموديلات هتشتغل. كمان الـ ONNX ecosystem أوسع من ناحية الـ tooling والـ optimization.

### 5. الأبلكيشن بيشتغل offline؟

جزئيا. القاموس أوفلاين كامل، 1,023 علامة في Room database محلي. الـ ML inference أوفلاين عن طريق ONNX Runtime. بس الـ Thoth chat والـ Explore images محتاجين نت عشان بيكلموا الـ backend API.

في الخطة الجاية هنزل الموديلات كلها جوا الـ APK عشان الـ scan يشتغل 100% offline.

### 6. ايه اللي كان صعب؟

أصعب حاجة كانت الـ 18-module architecture. إزاي تخلي الـ feature modules تتكلم مع بعض من غير ما تعرف عن بعض directly. الحل كان الـ domain layer كـ contract layer والـ Compose Navigation 2.8+ مع type-safe routes.

كمان الـ token leak كان tricky. كان bug خفي، مش بيعمل crash بس كان security issue حقيقي. اكتشفناه لما بقينا بنشوف الـ network requests بالتفصيل.

### 7. ايه الخطة الجاية؟

أهم 3 حاجات: live camera scan مع AR، deep translation عشان نفهم جمل كاملة مش حروف بس، وشراكات مع متاحف. في الـ longer term: education mode للمدارس ونزود الأساطير لـ 50+.
