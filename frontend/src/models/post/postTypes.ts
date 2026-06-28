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
};

export type CreatePostDTO = Pick<Post, "title" | "content"> & { boardId: number };

export type BrifePostDTO = Pick<Post, "title" | "content">;

export type DeletePostDTO = Pick<Post, "id">;

export type RestorePostDTO = Pick<Post, "id">;

export type PostEditDTO = BrifePostDTO & BriefBoardVO;

export type RecentPostVO = Pick<
  Post,
  "id" | "title" | "content" | "likeCount" | "commentCount" | "createTime"
> & {
  boardName: string;
  boardCover: string;
};

export type GetPostsDTO = PageRequestDTO & {
  boardId?: number;
};
