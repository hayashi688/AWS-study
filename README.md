# AWS学習RPG — 200問・ランダム出題

Java 21 / Spring Boot 3.5.7 / Maven / Thymeleaf / Vanilla JavaScript。
DB・AWS接続・AI・音声機能は使用しません。

## 起動

Java 21 JDKが必要です。STSは File → Import → Maven → Existing Maven Projects でフォルダを開き、`AwsStudyApplication.java` を右クリック → Run As → Spring Boot App。
VS Codeは Extension Pack for Java と Spring Boot Extension Pack を入れ、プロジェクトフォルダを開き、起動クラスのmain上のRunを押します。

Mavenをインストール済みなら、リポジトリルートで：

```sh
mvn clean package
mvn spring-boot:run
```

ビルド済みJARを使う場合：

```sh
java -jar target/aws-study-0.0.1-SNAPSHOT.jar
```

PC上で起動後、ブラウザで `http://localhost:8080/` を開きます。クラウドのlocalhostはPCのlocalhostとは別です。

クラウド内でのビルド：

```sh
JAVA_HOME=/workspace/tools/jdk-21 \
MAVEN_OPTS=-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts \
/workspace/tools/apache-maven-3.9.11/bin/mvn \
  -s /workspace/tools/maven-settings.xml \
  -Dmaven.repo.local=/workspace/.m2/repository -B clean package
```

## 出題・保存

200問のIDをFisher-Yatesで一度だけシャッフルし、保存した順番で出題します。不正解時は同じ問題を維持し、HPも変更しません。正解するとHP 0・CLEARになり、「次の敵へ」で進めます。全問正解するとGAME CLEAR。「最初から遊ぶ」で進捗をリセットし、再シャッフルします。

localStorageの `aws-rpg-progress` に以下を保存します。

- `order`: シャッフル済みID配列
- `currentIndex`: 現在の位置
- `clearedIds`: 正解済みの一意なID
- `completed`: 完了状態
- `datasetVersion`: schemaVersionとJSON全体のSHA-256

正解直後にリロードしてもHP 0と次の敵へボタンを復元します。全問クリアも復元します。
進捗は正解済みのユニークなID数から計算します（例：37 / 200問、18.5%）。ID番号や出題位置からは計算しません。

古いデータセット、無効なID、重複順序、不正な位置などは、有効な正解済みIDを保持し、残りの問題をシャッフルして順序を再構築します。JSONとして読めない保存内容は新規開始します。localStorageが利用不可・容量超過でもプレイは可能で、保存できないことを表示します。
保存はブラウザ・サイトのオリジン単位です。別PC・別ブラウザ・別ポートには共有されません。従来の100問版には保存機能がないため、保存されていない過去の進捗は復元できません。保存済みの100問形式（同じフィールド）の有効IDは200問へ移行できます。

## 冒険者の書

各問題の開始時はカテゴリ一覧を表示します。10カテゴリ → 所属サービス → 登録済みの解説項目 → 解説本文の4階層です。各階層から前の一覧に戻れます。6種類の解説項目のうち、本文が登録されている項目だけを表示します。

冒険者の書は出題中の問題と独立した辞典です。全カテゴリ・全サービスを自由に閲覧でき、今回の問題の強調マークはありません。問題固有の回答例は回答欄に表示します。各解説のAWS公式リンクは別タブで開きます。

## 回答判定

当初MVPのキーワード包含判定に戻しました。`keywords` のいずれかを含む回答は正解です。
添付の14問ではsampleAnswerにキーワードが含まれないため、回答例との一致も正解にします。回答例比較では全半角・英字の大小・空白・末尾の句点・「だ／です」の差を吸収します。
意味解析はしないため、キーワードを含む否定文も正解になります。JSONのjudgeNoteは説明用データであり、実行指示としては扱いません。
空回答・2000文字超過・不明IDはHTTP 400です。

## データ編集

`src/main/resources/aws_rpg_200_questions_with_categories.json` を編集します。Javaの修正は不要です。

- `questions`: idは一意、stageは1からの連番、totalは件数と一致
- `serviceCategories`: カテゴリID・表示名・所属サービス名
- `serviceCatalog`: serviceNameとsections内の解説entries
- `navigationSections`: 項目ID・表示名・説明
- `quizSettings`: 現在は全問・重複なしシャッフル・localStorage保存・再プレイでリセット

問題のadventureBook.serviceNameはserviceCatalogのサービス名と一致させてください。公式リンクはHTTPSのdocs.aws.amazon.com URLです。追加・修正後は再ビルド・再起動します。JARにはビルド時のJSONが同梱されます。データ変更時はバージョンが変わり、進捗が移行されます。

## API・ファイル

- `GET /`: Thymeleaf画面
- `GET /api/battle/dataset`: データセットとバージョン
- `GET /api/battle/questions`: 問題一覧（既存API維持）
- `POST /api/battle/answer`: `{ "questionId": "AWS-001", "answer": "オブジェクト" }`

Javaは `src/main/java/com/example/awsstudy/` 配下です。

| ファイル | 役割 |
| --- | --- |
| AwsStudyApplication.java | 起動 |
| controller/GameController.java | 初期画面 |
| controller/BattleController.java | データ取得・回答API |
| service/QuestionCatalog.java | JSON読込・整合性チェック・バージョン生成 |
| service/BattleService.java | 問題選択・HP・結果生成 |
| service/AnswerJudgeService.java | 将来Bedrockへ差し替える判定インターフェース |
| service/KeywordAnswerJudgeService.java | キーワード・回答例判定 |
| model/Question.java、Enemy.java、dto/* | 問題・敵・API入出力 |
| resources/templates/game.html | ゲーム画面 |
| resources/static/css/game.css | RPG風デザインとスマートフォン表示 |
| resources/static/js/game.js | 戦闘・冒険者の書・保存連携 |
| resources/static/js/progress.mjs | シャッフル・保存状態の検証・復旧 |
| scripts/progress.test.mjs | 出題順・復元・移行のNodeテスト（外部依存不要） |

## 確認

```sh
mvn test
node --test scripts/progress.test.mjs
```

画面では、任意の問題で「わかりません」→MISS・HP維持・同じ問題、回答例→HP 0・CLEAR、リロード→CLEAR復元、次の敵→別の問題、カテゴリ→サービス→登録済み項目→解説、全200問→最終クリアを確認します。
将来の音声入力は認識した文字列を同じ回答APIに送れます。
