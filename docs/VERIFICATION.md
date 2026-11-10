# RabbitTools 验证记录

环境：Minecraft 1.21.1、NeoForge 21.1.265、Java 21。
日期：2026-11-08。

## 已完成

- RabbitTools `gradlew.bat build --offline` 编译、打包通过。
- RabbitMine 移除旧采矿锤后，`gradlew.bat build --offline` 通过。
- `gradlew.bat runData --offline` 通过；中英文采矿锤翻译保留在生成结果中。
- 资源 JSON 可解析、无重复资源路径，建筑杖及采矿锤模型的贴图与覆盖模型引用完整。
- 仅安装 RabbitTools、NeoForge 的本机专用服务端成功启动，并再次加载同一测试世界。
- 加载 1306 个配方、1421 个进度，未出现模组配方或进度解析错误。
- 通过 RCON 逐个生成并读取实际物品 ID，4 把采矿锤、7 根建筑杖、3 个核心、虚空袋共 15 个物品通过。
- 执行 `save-all flush` 和 `stop`，测试服务器正常保存并关闭。

`build/registration-smoke.txt` 保留该次注册检查结果。
`src/main/resources` 的采矿锤配方、模型和标签手工维护；建筑杖资源和语言由 `runData` 生成。

## 注册检查复现

仅对本机开发服务器使用：在 `run/server/server.properties` 绑定 `127.0.0.1`，
启用 RCON，设端口为 25586，并设置本地临时密码。启动 `gradlew.bat runServer`，
看到 Done 后运行 `scripts/Test-Registrations.ps1`。
脚本读取本地密码，生成临时物品验证注册，然后保存世界并关闭测试服务器。
密码和测试世界均位于忽略的 run 目录，不包含在源码或 jar 中。

## 尚未人工验证

构建任务没有 Java 单元测试，构建通过不代表下列游戏内行为已实测：

1. 生存模式持锤挖掘水平和垂直 3×3 平面，检查耐久、掉落、潜行单块挖掘及范围描边。
2. 对锤使用效率、耐久、时运、精准采集、经验修补，核对效果。
3. 各档建筑杖放置不同朝向方块，检查材料与耐久扣除、随机放置、限制选项及范围预览。
4. 安装天使、破坏、交换核心，切换核心后检查对应行为与材质变化。
5. 从背包、副手、潜影盒和收纳袋供料；材料不足时应停止，不能生成免费方块。
6. 撤销放置、破坏及交换，核对返还/消耗和方块状态。
7. 虚空袋开关、GUI、收集和连接容器；退出并重进世界后核对物品数据。
8. 多人连接时服务端配置同步、两人同时操作及重启后的工具状态。

上游的撤销历史是运行期数据，不承诺跨服务器重启保留。
本次不做旧 ID 的存档迁移，也不测试第三方模组的专用适配。
## 空白核心与中间组件更新验证（2026-11-08）

- 新增两种组件及空白核心后，数据生成和最终构建通过。
- 本机专用服务端启动成功，加载 1308 个配方和 1423 个进度，无模组配方解析错误。
- RCON 检查全部 18 个物品 ID 通过，测试服务器正常保存并关闭。
- 三种特殊核心的生成配方及 jar 内配方均恰好需要 1 个空白核心，原材料数量保持不变。
- 检查确认空白核心没有自身合成配方；模型、中英文名称及 16×16 透明贴图已打包。
- 核心安装流程未增加空白核心开销，也未添加返还、绑定或自动发放逻辑。
- 新配方的实际工作台制作、批量合成和安装仍需人工进游戏验证。

## 核心配方布局调整（2026-11-08）

空白核心移至中央；原中心材料移至左上和右下，由 1 份增为 2 份。
三种配方统一采用 O#X / #B# / X#O，B 为空白核心，O 为原中心材料，X 为原角落材料，# 为玻璃板。
此调整取代上一节原材料数量不变的说明。

## 杖头制作路线验证（2026-11-08）

- 新增铁、金、钻石、下界合金、无限五种杖头；参考原三种功能核心，由 imagegen 生成并导出 16×16 RGBA 纹理。
- 数据生成及 build --offline 通过，物品模型、配方、贴图均已核对进入最终 jar。
- 铁/金杖头各需 1 建造组件及 2 对应金属锭；钻石杖头需 1 精密建造组件及 2 钻石。
- 下界合金杖头使用钻石杖头、下界合金升级模板、下界合金锭，通过原版锻造配方制作。
- 无限杖头需下界合金杖头、精密建造组件及下界之星各 1 个。
- 五档成品手杖各仅有 1 个配方，必须消耗对应杖头和 2 根木棍，旧直接材料配方已被替换。木石配方保留。
- 空白核心仍无合成配方，特殊核心配方及安装逻辑不受杖头影响。
- 本机专用服务端启动成功，加载 1313 个配方及 1428 个进度，无模组配方解析错误。
- RCON 检查全部 23 个物品 ID 通过，测试服务器已正常保存并关闭。
- 尚未人工验证实际工作台合成、锻造界面操作和新贴图的游戏内显示。

## 贴图重新绘制与换色验证（2026-11-08）

- 替换建造组件、精密建造组件、空白核心及五种杖头共 8 张贴图。
- 构件以 Create 精密构件作结构参考重绘；原始 Create PNG 未打包。
- 杖头套用原天使核心像素模板及 imagegen 材质配色，逐像素核对五张杖头的 alpha 与原天使核心完全一致。
- 八张图均为 16×16 RGBA，已与旧图比较确认替换；jar 内 PNG 的 SHA-256 与项目文件一致。
- 23 个配方 JSON 的 SHA-256 均保持不变，本次未改动 Java 行为。
- build --offline 通过，未重复运行无关服务端测试；游戏内实际显示尚未人工验证。
- 源图、完整提示词、旧图备份及预览见 docs/art/retexture-v2。

## 简洁构件贴图验证（2026-11-08）

- 三种构件改为核心风格的厚边、大色块和单个中心接点，不再保留细密齿轮图案。
- 三张图均为 16×16 RGBA、居中的 10×10 主体、纯 0/255 alpha，最多 8 种不透明颜色，透明轮廓一致。
- 人工查看最终原生贴图的最近邻放大预览；源图、配色表及原生拼图保存在 docs/art/components-v3。
- jar 中 PNG 的 SHA-256 与最终本地文件完全一致，23 个配方文件均未变化。
- build --offline 通过；本次只替换三种构件贴图，五种杖头保留，未进游戏人工验证。

## 现代原版调性色彩统一（2026-11-08）

- 调整三种构件和五种杖头，保留原生 16×16 分辨率与逐像素透明轮廓。
- 以本地 Minecraft 1.21.1 材质为色彩与光照参考；控制极端明暗，采用冷暖色相偏移及选择性有色边缘。
- 全部最终 RGB 属于有记录的材质色板，不含纯黑或纯白不透明像素。
- 已查看最终最近邻放大预览；美术风格匹配属于人工判断，不将程序检查当作风格或游戏内效果证明。
- 八张 PNG 均已改变，jar 内 SHA-256 与本地最终文件一致；23 个配方 SHA-256 均未变化。
- build --offline 通过；本次未改 Java 行为，也未重复启动服务端或进游戏实测。
- 美术规范见 docs/ART_STYLE.md；源图、完整提示词、色板及前后预览见 docs/art/modern-vanilla-v4。

## 传送手杖验证

- Minecraft 1.21.1 / NeoForge 21.1.265，数据生成和构建通过。
- 本机开发专用服务端加载 1315 个配方。临时夹具调用手杖的服务端使用入口，验证平地、高台、空气、玻璃墙、身体路径空间、两档距离、耐久、共享冷却与禁用。夹具模拟了玩家位置更新，未验证客户端网络传送与实际手持显示。
- 执行禁用指令并保存，正常关闭和重启后状态仍为禁用。启用指令成功恢复状态。
- 两张 16×16 贴图保留建筑手杖原杖身和透明轮廓。imagegen 生成末影珍珠配色设计，再将杖头中心取样到原生像素。

生成提示词：

- diamond: Edit the reference Minecraft 16x16 diamond building wand sprite to make a RabbitTools travel staff. Preserve the diagonal shaft and exact small original silhouette, length and transparent padding. Change only the material at the upper-right tip into a small ender-pearl teal-green inset with a darker blue-green edge and one restrained warm highlight. Keep the tier's diamond cyan outer metal around the pearl. Modern vanilla Minecraft item palette, compact clean pixel clusters, no outline glow, no particles, no text, no new shaft. Strict logical 16x16 sprite, enlarged with nearest neighbor and transparent background. The original lower shaft must remain visually the same.
- netherite: Edit the reference Minecraft 16x16 netherite building wand sprite to make a RabbitTools travel staff. Preserve the diagonal shaft and exact small original silhouette, length and transparent padding. Change only the material at the upper-right tip into a small ender-pearl teal-green inset with a darker blue-green edge and one restrained warm highlight. Keep the tier's dark warm-gray netherite outer metal around the pearl. Modern vanilla Minecraft item palette, compact clean pixel clusters, no outline glow, no particles, no text, no new shaft. Strict logical 16x16 sprite, enlarged with nearest neighbor and transparent background. The original lower shaft must remain visually the same.

## 传送手杖穿墙与配方调整

- 开发专用服务端加载 1315 个配方。临时服务端夹具以平视方向使用手杖，验证平地落点、穿过实心墙、高台、身体空间不足、危险地面、悬空、两档距离、耐久、共享冷却与禁用。
- 夹具调用物品的服务端使用入口并模拟位置更新，客户端联机操作留待实测。

## 砌筑铲、测量尺和上色刷

- Java 21 / Minecraft 1.21.1 / NeoForge 21.1.265。数据生成和构建通过，JAR 中核对三份配方和四张纹理。
- 开发专用服务端验证砌筑铲放置、材料与耐久、测量尺选点与清除、刷子空库存、补色保留选色、64 点容量、实际刷色扣点，以及被取消的放置与刷色。关闭调色盘会返还未转化的染料。
- 英文和中文开发客户端连接本机专用服务端，验证调色盘打开、Shift 点击移入染料、装填一份、装填至满、余额同步及关闭后染料返还。已查看中文界面实机截图。测量尺的世界标记尚未做单独实机视觉检查。
- 三个物品图标按 16×16 像素逐格绘制，并查看原尺寸和最近邻放大预览。调色盘沿用原版容器的斜面与库存槽，使用暖灰底和褐紫暗边。

## 测量尺几何中心

- 检查实际几何方法在平面、立体、奇偶边长、负坐标和对调端点时返回的精确坐标和中心方块范围。
- 数据生成与构建通过，JAR 已核对中英文中心标签。世界标记尚未做游戏内视觉检查。
