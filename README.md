# Echo Core (Forge 1.20.1)

Uçan, zeki bir yapay zekâ yoldaşı: **ECHO**. Seni korur, düşmanları işaretler, maden tarar. Ama kendi geçmişini hatırlamıyor ve bir şey ona seni hatırlatıyor.

## Nasıl derlenir
1. Java 17 kur.
2. Forge 1.20.1 MDK'yı indir (files.minecraftforge.net, sürüm 47.x).
3. MDK'daki `gradlew`, `gradlew.bat` ve `gradle/` klasörünü bu klasöre kopyala.
4. Terminalde: `./gradlew build`
5. `build/libs/echocore-1.0.0.jar` dosyasını Minecraft `mods` klasörüne at (Forge 1.20.1 gerekir).

## Otomatik test
`.github/workflows/build.yml` hem derler hem de modu gerçek Forge sunucusunda yükleyip "Server smoke test" yapar. Test başarısız olursa Actions'ta kırmızı çarpı çıkar ve hata satırları o adımın çıktısında yazar. Jar yalnızca test geçerse yüklenir. Kayıtlar `server-logs` dosyasında saklanır.

## Oyunda
- **Echo Çekirdeği** (tarif: demir, ametist parçası, kızıltaş, Ender Gözü) ya da **Echo Doğma Yumurtası** ile ECHO'yu çağır.
- Boş elle sağ tık: **tarama** (yakındaki cevheri koordinatla söyler, 30 sn bekleme).
- Eğilip boş elle sağ tık: **bekleme modu** aç/kapat.
- Düşmanları otomatik **parlatır**, creeper'a özel uyarı verir, geceyi haber verir.
- Aralıklarla **glitch** yaşar ve bir **Hafıza Parçası** bırakır. Parçayı ECHO'ya sağ tıkla: 7 bölümlük gizem açılır.
- 7. parçada ECHO **evrim** geçirir (60 can, daha güçlü, sana yenilenme verir).
- ECHO ölürse Echo Çekirdeği düşürür; hafızası çekirdeğin içinde saklanır.

## Animasyonlar
Süzülme, harekette öne yatma, dönen yan kapsüller, anten sallanması, saldırıda öne atılma, bekleme modunda kapsülleri toplama, glitch titremesi (kırmızı gözler), ölümde dönerek düşme. Gözler, anten ucu ve itici karanlıkta parlar.
