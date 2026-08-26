# rich-text-support 任务拆分

说明：
- 本需求已确认帖子支持文字、emoji、正文图片，评论仅支持文字和 emoji。
- 设计文档里提到的评论长度上限，当前代码需要先核对再固化；以下任务不额外扩展新的内容规则。

## 1. 确认并固化评论输入规则
- 任务目标：先核对现有评论长度上限与空白判断口径，确保“仅 emoji 可发”“纯空白不可发”，避免后续帖子富文本改造时把评论规则带偏。
- 涉及模块/文件：`backend/src/main/java/sim/forum/dto/comment/CommentDTO.java`，`backend/src/main/java/sim/forum/service/impl/CommentServiceImpl.java`，`backend/src/main/java/sim/forum/controller/CommentController.java`，`backend/src/test/java/sim/forum/service/CommentServiceTest.java`
- 完成标准：评论提交在后端完成统一校验；仅空白内容被拒绝；仅 emoji 评论可正常落库；评论删除、恢复、消息和计数逻辑不受影响。
- 前置依赖：无

## 2. 建立帖子内容模型与安全解析
- 任务目标：为帖子正文建立受限节点模型和解析能力，兼容历史纯文本，拒绝非法 JSON、外部 URL、HTML 片段和不支持的节点类型。
- 涉及模块/文件：`backend/src/main/java/sim/forum/dto/post/CreatePostDTO.java`，新增 `backend/src/main/java/sim/forum/dto/post/content/*` 或同职责包，`backend/src/main/java/sim/forum/service/impl/PostServiceImpl.java`，`backend/src/main/java/sim/forum/vo/post/PostVO.java`，`backend/src/main/java/sim/forum/vo/post/RecentPostVO.java`
- 完成标准：`POST /posts` 可接受新节点 JSON 和历史纯文本；详情、列表、最近浏览都能输出规范化后的正文展示数据；正文长度与有效性校验在入库前完成。
- 前置依赖：任务 1

## 3. 增加帖子正文图片上传接口
- 任务目标：补齐帖子正文图片上传能力，沿用本地文件上传和 `/uploads/**` 静态资源映射，只返回相对路径。
- 涉及模块/文件：`backend/src/main/java/sim/forum/controller/PostController.java`，`backend/src/main/java/sim/forum/service/FileUploadService.java`，`backend/src/main/java/sim/forum/config/WebConfig.java`，必要时补充 `backend/src/main/resources/application.yaml.example`
- 完成标准：新增 `POST /posts/images`；要求登录；仅允许 `jpg/jpeg/png`；校验实际图片类型和单文件大小；保存到当前用户帖子正文目录；失败时不产出可写入正文的路径。
- 前置依赖：任务 2

## 4. 改造帖子发布编辑器
- 任务目标：把发帖区域从纯 textarea 改成支持文字、emoji、图片插入的正文编辑器，并在提交前序列化为帖子内容节点 JSON。
- 涉及模块/文件：`frontend/src/components/post/PostEdit.vue`，新增 `frontend/src/components/post/PostContentEditor.vue`，`frontend/src/api/post.ts`，`frontend/src/models/post/postTypes.ts`
- 完成标准：支持输入文字和 emoji；支持选择文件和拖拽本地图片；图片上传成功后插入到当前光标位置；上传失败不插入残缺节点；发帖失败后保留当前编辑内容。
- 前置依赖：任务 2、任务 3

## 5. 改造帖子详情、列表和最近浏览展示
- 任务目标：用安全渲染器替换现有 `v-html`，让帖子正文在详情页、列表页和最近浏览中按顺序展示文字、emoji、换行和图片。
- 涉及模块/文件：新增 `frontend/src/components/post/PostContentRenderer.vue`，`frontend/src/views/posts/PostDetail.vue`，`frontend/src/components/post/PostCard.vue`，`frontend/src/components/post/RecentPostCard.vue`，`frontend/src/models/post/postTypes.ts`
- 完成标准：页面不再直接渲染原始 HTML；文本和换行可读；图片按块级布局展示，加载失败只影响当前图片；列表和最近浏览使用摘要与首图，不破坏布局。
- 前置依赖：任务 2、任务 4

## 6. 做评论区联调回归
- 任务目标：确认评论输入、展示和回复链路在富文本改造后仍然稳定，尤其是 emoji-only 评论和现有回复流程。
- 涉及模块/文件：`backend/src/main/java/sim/forum/service/impl/CommentServiceImpl.java`，`frontend/src/views/posts/PostDetail.vue`，`frontend/src/components/comment/CommentCard.vue`，`frontend/src/components/comment/SubCommentItem.vue`
- 完成标准：评论仍按现有流程发布、分页、删除、恢复和通知；仅 emoji 评论可正常发布和显示；现有 `pre-wrap` 展示不被破坏。
- 前置依赖：任务 1、任务 2

## 7. 补充测试与验收
- 任务目标：用后端测试和页面回归覆盖富文本正文、图片上传、历史纯文本兼容和评论 emoji 规则。
- 涉及模块/文件：`backend/src/test/java/sim/forum/service/PostServiceTest.java`，`backend/src/test/java/sim/forum/service/CommentServiceTest.java`，必要时新增帖子内容解析或图片上传相关测试类
- 完成标准：覆盖新旧帖子正文格式、非法节点拒绝、图片上传失败路径、emoji-only 评论、详情/列表/最近浏览展示和现有删除恢复回归；构建或相关测试可通过。
- 前置依赖：任务 2、任务 3、任务 4、任务 5、任务 6
