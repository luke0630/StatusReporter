StatusReporter
====

MinecraftServerManagerにサーバーの状態を送信したり、  
他のサーバーの状態を取得してサーバー内に表示できます。(PlaceholderAPI)

# 特徴
> * PlaceholderAPIに対応
> * プレイヤーリスト
> * バージョン、サーバーのステータス(起動中、稼働中)
> * プラグインリスト(バージョン、概要、製作者、メインクラス)

# インストール
Releasesから以下の最新バージョンをダウンロード  
`statusreporter-x.x.x.jar`  
<https://github.com/luke0630/StatusReporter/releases>

サーバーのpluginsフォルダにjarファイルを入れてください。
# PlaceholderAPIの使用
PlaceholderAPIをpluginsフォルダに入れておいてください。  
<https://github.com/PlaceholderAPI/PlaceholderAPI>

```
オフライン/オンラインの状態 : %status_サーバーの名前%
サーバーのプレイヤー数 : %status_サーバーの名前_playerscount%
サーバーのバージョン : %status_サーバーの名前_version%
```
# ライセンス
[MIT licensed](./LICENSE).