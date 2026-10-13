# AWS学習RPG — 100問

Java 21 / Spring Boot 3.5.7 / Maven 3.9.11 / Thymeleaf / Vanilla JavaScript。
DB、AWS接続、AI、音声機能は使用しません。

## 起動

Java 21とMavenを準備し、リポジトリのルートで実行します。

```sh
mvn test
mvn spring-boot:run
```

ローカルPCのブラウザで `http://localhost:8080/` を開きます。
クラウド環境内のループバックURLを外部公開する設定は含みません。

このクラウド環境では次のコマンドを使います（ネットワークプロキシ対応）。

```sh
JAVA_HOME=/workspace/tools/jdk-21 \
MAVEN_OPTS=-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts \
/workspace/tools/apache-maven-3.9.11/bin/mvn \
  -s /workspace/tools/maven-settings.xml \
  -Dmaven.repo.local=/workspace/.m2/repository test
```

パッケージ済みアプリの起動：

```sh
java -jar target/aws-study-0.0.1-SNAPSHOT.jar
```

## ゲーム確認

1. 初期画面でS3 スライム、HP 100、進捗 1 / 100、冒険者の書を確認します。
2. 「ブロックストレージです」を入力して攻撃すると、MISS、HP 100のままになります。
3. 「S3はオブジェクトストレージです」を入力して攻撃すると、100ダメージ、HP 0、CLEARが表示されます。
4. 画面のリロードが発生せず、クリア後は攻撃ボタンが無効になります。
5. 「次のステージへ」で進み、敵の顔文字・発言・冒険者の書・公式リンクの切り替えを確認します。
6. 100問目を倒すとGAME CLEAR画面になります。「最初から遊ぶ」で再開できます。
7. ブラウザをリロードすると初期状態に戻ります。進捗の永続保存はありません。

APIは `POST /api/battle/answer`。JSONで `questionId: "AWS-001"` と `answer` を送ります。
空回答、2000文字を超える回答、不明な問題IDはHTTP 400になります。
キーワード包含ではなく、JSONのsampleAnswer全体との限定的一致で判定します。全半角、英字の大小、空白、末尾の句点と「だ／です」の違いだけ許容します。否定・主語・助詞・語順・追加文はそのまま比較し、判断できない回答はWRONGとして再回答を促します。自然言語の言い換え全般には対応しません。画面の「回答例・判定方法」を参照してください。
セーブやセッション管理はなく、APIは各回答を初期HP 100から判定します。

## ファイルの役割

| ファイル（Javaは src/main/java/com/example/awsstudy/ 配下） | 役割 |
| --- | --- |
| pom.xml | Spring Boot、Web、Thymeleaf、入力検証、テスト依存関係 |
| .gitignore | Maven生成物を除外 |
| AwsStudyApplication.java | 起動クラス |
| controller/GameController.java | 初期画面と敵・問題データを渡す |
| controller/BattleController.java | 回答APIと入力検証 |
| service/BattleService.java | JSONから問題選択、ダメージ計算、結果生成 |
| service/QuestionCatalog.java | JSON読込、問題数・ID・ステージ・公式URLの検証 |
| src/main/resources/aws_rpg_100_questions.json | 添付の全100問データ |
| service/AnswerJudgeService.java | 回答判定の差し替え用インターフェース |
| service/KeywordAnswerJudgeService.java | サンプル回答との限定的一致判定 |
| model/Enemy.java | 敵情報 |
| model/Question.java | 問題・正解・キーワード・学習資料 |
| dto/AnswerRequest.java | API入力 |
| dto/AnswerResponse.java | API結果 |
| src/main/resources/templates/game.html | Thymeleafゲーム画面 |
| src/main/resources/static/css/game.css | RPG風の2カラム・スマートフォン表示、CSSスライム |
| src/main/resources/static/js/game.js | Fetchによる回答送信、HP・セリフ・結果更新、送信中とクリア後の操作制御 |
| src/test/java/com/example/awsstudy/BattleApplicationTest.java | 画面描画、正解・不正解、入力エラーの統合テスト |
| README.md | 起動・確認・設計説明 |

## 将来の差し替え箇所

Bedrock: `AnswerJudgeService` を実装するサービスを追加し、`KeywordAnswerJudgeService` の代わりに注入します。`BattleService` のダメージ計算やAPIの入出力は維持できます。AWS SDKや認証設定はその実装時に追加します。

音声入力: `game.html` にマイク操作を追加し、`game.js` で音声認識結果を回答欄に入力します。認識後の文字列は同じ回答APIに送信でき、バックエンドの契約は変更不要です。

## 問題データの編集

`src/main/resources/aws_rpg_100_questions.json` のquestionsを編集します。Javaコードの変更は不要です。totalは配列の件数、stageは1からの連番、idは一意にしてください。enemy、adventureBook、sampleAnswerを設定します。officialDocUrlはHTTPSのdocs.aws.amazon.com URLです。JSONのjudgeNoteは説明用データで、実行命令としては扱いません。

編集後は再ビルド・再起動してください。JARにはビルド時のJSONが同梱されます。件数はデータから決めるため100問以外にも対応できます。GET /api/battle/questionsで一覧を取得します。

STS: File → Import → Maven → Existing Maven Projectsから開き、AwsStudyApplication.javaを右クリック → Run As → Spring Boot App。
VS Code: Extension Pack for JavaとSpring Boot Extension Packを入れ、プロジェクトフォルダを開いてAwsStudyApplicationのmain上のRunを押します。どちらもJava 21 JDKが必要です。
