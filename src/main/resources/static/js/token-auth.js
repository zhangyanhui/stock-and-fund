/**
 * Token认证脚本
 * 自动为所有API请求添加Authorization请求头
 */

// 固定的Token，与后端SimpleTokenFilter中的Token保持一致
const AUTH_TOKEN = 'Bearer stock-fund-app-secret-token-2026-03-01-xyz789';

// 确保在DOM加载完成后执行
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTokenAuth);
} else {
    initTokenAuth();
}

function initTokenAuth() {
    console.log('Token认证脚本开始初始化，Token:', AUTH_TOKEN);
    
    // 重写XMLHttpRequest的open方法，添加Token
    const originalOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
        console.log('XMLHttpRequest.open被调用，添加Token到请求:', url);
        originalOpen.call(this, method, url, async, user, password);
        try {
            this.setRequestHeader('Authorization', AUTH_TOKEN);
            console.log('Token已添加到XMLHttpRequest请求');
        } catch (e) {
            console.error('添加Token到XMLHttpRequest失败:', e);
        }
    };
    
    // 重写fetch方法，添加Token
    if (window.fetch) {
        const originalFetch = window.fetch;
        window.fetch = function(url, options) {
            console.log('fetch被调用，添加Token到请求:', url);
            options = options || {};
            options.headers = options.headers || {};
            options.headers['Authorization'] = AUTH_TOKEN;
            console.log('Token已添加到fetch请求');
            return originalFetch(url, options);
        };
    } else {
        console.log('fetch API不可用，跳过fetch方法重写');
    }
    
    // 重写jQuery的ajax方法，添加Token
    if (typeof $ !== 'undefined' && $.ajax) {
        console.log('jQuery ajax方法被重写，添加Token支持');
        $(document).ajaxSend(function(event, xhr, settings) {
            console.log('jQuery ajax请求被发送，添加Token到请求:', settings.url);
            try {
                xhr.setRequestHeader('Authorization', AUTH_TOKEN);
                console.log('Token已添加到jQuery ajax请求');
            } catch (e) {
                console.error('添加Token到jQuery ajax失败:', e);
            }
        });
    } else {
        console.log('jQuery未加载，跳过ajax方法重写');
    }
    
    console.log('Token认证已初始化完成');
    
    // 测试Token是否能被正确获取
    console.log('测试Token值:', AUTH_TOKEN);
    console.log('Token长度:', AUTH_TOKEN.length);
}

// 全局变量，方便其他脚本使用
window.AUTH_TOKEN = AUTH_TOKEN;
console.log('Token已设置为全局变量');

