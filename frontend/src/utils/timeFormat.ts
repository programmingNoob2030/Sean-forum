/**
 * 格式化发布时间
 * @param timeStr 时间字符串
 * @param isPrecise 是否在超过24小时后显示精确时间 (true: yyyy-MM-dd HH:mm:ss, false: yyyy-MM-dd)
 */
export function formatPostTime(timeStr: string, isPrecise: boolean = false): string {
  const postDate = new Date(timeStr);
  const now = new Date();

  // 1. 基础参数检查：如果解析失败，原样返回或返回空
  if (isNaN(postDate.getTime())) return timeStr;

  const diff = now.getTime() - postDate.getTime();
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  // 辅助函数：补零
  const pad = (n: number) => (n < 10 ? '0' + n : n);
  
  // 预取时间分量
  const y = postDate.getFullYear();
  const m = pad(postDate.getMonth() + 1);
  const d = pad(postDate.getDate());
  const hh = pad(postDate.getHours());
  const mm = pad(postDate.getMinutes());
  const ss = pad(postDate.getSeconds());

  // --- 逻辑判断分水岭 ---

  // 情况 A: 1小时内 (人性化)
  if (diff < hour) {
    const minNum = Math.floor(diff / minute);
    return `${minNum <= 0 ? 1 : minNum} 分钟前`;
  } 
  
  // 情况 B: 24小时内 (人性化)
  if (diff < day) {
    return `${Math.floor(diff / hour)} 小时前`;
  }

  // 情况 C: 超过24小时 (根据参数决定精度)
  if (isPrecise) {
    // 满足你的需求：超过24小时后显示“精确到秒”
    return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
  } else {
    // 默认显示：仅日期
    return `${y}-${m}-${d}`;
  }
}