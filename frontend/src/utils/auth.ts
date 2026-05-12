import router from '@/router';
import { useUserStore } from '@/models/user/userStore'; // 确保路径指向你的 Pinia store
import { ElMessage } from 'element-plus';
import { nextTick } from 'vue';

/**
 * 权限控制工具函数
 * @param action 登录后需要执行的操作 (回调函数)
 * @param options 配置项
 */
export const ensureLogin = async (action: () => void, options = { redirect: true }) => {
  const userStore = useUserStore();

  if (userStore.isLoggedIn) {
    action();
  } else {
    ElMessage.warning('此操作需要登录，请先登录社区');
    
    if (options.redirect) {
      // 1. 确保获取的是最新的路径
      const currentPath = router.currentRoute.value.fullPath;
      
      // 2. 等待路由准备就绪
      await router.isReady();
      
      // 3. 尝试跳转
      try {
        await router.push({
          path: '/login',
          query: { redirect: currentPath }
        });
        
        // 兜底方案：如果 router.push 还是没反应（比如在某些奇怪的 Edge Case 下）
        // 可以检查当前 URL 是否真的变了，没变则强制跳转
        if (window.location.hash.indexOf('/login') === -1 && window.location.pathname !== '/login') {
             // 只有在 push 彻底失效时才考虑用这个
             // window.location.href = `/#/login?redirect=${encodeURIComponent(currentPath)}`;
        }
      } catch (err) {
        console.error('路由跳转失败:', err);
      }
    }
  }
};