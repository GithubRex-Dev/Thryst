# Thryst Life Style App
采用MVC的设计方案，UI设计力求简约
包含四个模块  Recipe、HowTo、收藏列表和购物清单
视图主要采用 ListView+CardView 的模式
控制层主要在各个listAdapter中实现，
数据库采用本地Sqlite数据库，保存用户个人操作信息；云端采用FireBase的 Realtime Database 和 Storage. 
视频播放采用本地代理模式，边播放边缓存。
步骤说明清单、配方清单 使用本地代理缓存
