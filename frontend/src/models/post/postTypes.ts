import type { BriefBoardVO } from "@/models/board/boardTypes";
import type { PageRequestDTO } from "@/models/pages";

export interface Post {
  id: number;
  creator: string;
  title: string;
  content: string;
  isDeleted: number;
  createTime: string;
  likeCount: number;
  commentCount: number;
}

export type PostVO = Post & {
  postRatingType: number;
  creatorName: string;
  boardName: string;
  boardCover: string;
  creatorAvatar: string;
  boardId: number;
  contentFormat?: PostContentFormat;
  contentNodes?: PostContentNode[];
  contentTextPreview?: string;
  firstImagePath?: string;
};

export type CreatePostDTO = Pick<Post, "title" | "content"> & { boardId: number };

export type BrifePostDTO = Pick<Post, "title" | "content">;

export type DeletePostDTO = Pick<Post, "id">;

export type RestorePostDTO = Pick<Post, "id">;

export type PostEditDTO = BrifePostDTO & BriefBoardVO;

export type PostContentNodeType = "text" | "image";

export interface PostContentTextNode {
  type: "text";
  text: string;
}

export interface PostContentImageNode {
  type: "image";
  path: string;
}

export type PostContentNode = PostContentTextNode | PostContentImageNode;

export interface PostContentDocument {
  version: 1;
  nodes: PostContentNode[];
}

export type PostContentFormat = "PLAIN" | "BLOCKS";

export interface PostContentView {
  format: PostContentFormat;
  nodes: PostContentNode[];
  textPreview: string;
  firstImagePath?: string;
}

export type PostImageUploadResponse = string;

export type RecentPostVO = Pick<
  Post,
  "id" | "title" | "content" | "likeCount" | "commentCount" | "createTime"
> & {
  boardName: string;
  boardCover: string;
  contentFormat?: PostContentFormat;
  contentNodes?: PostContentNode[];
  contentTextPreview?: string;
  firstImagePath?: string;
};

export type PostSort = 'RECENT' | 'POPULAR' | 'COMMENTS' | 'HOT';

// 增加筛选sort参数
export type GetPostsDTO = PageRequestDTO & {
  boardId?: number;
  sort?: PostSort;
  keyword?: string;
};
