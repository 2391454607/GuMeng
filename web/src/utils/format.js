p/**
 * 格式化日期
 * @param {string|Date} date - 日期对象或日期字符串
 * @param {string} fmt - 格式化模板，例如：'yyyy-MM-dd hh:mm:ss'
 * @returns {string} - 格式化后的日期字符串
 */
export function formatDate(date, fmt = 'yyyy-MM-dd hh:mm:ss') {
  if (!date) return '';
  
  if (typeof date === 'string') {
    date = new Date(date);
  }
  
  const o = {
    'M+': date.getMonth() + 1, // 月份
    'd+': date.getDate(), // 日
    'h+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    'S': date.getMilliseconds() // 毫秒
  };
  
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
  }
  
  for (const k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length)
      );
    }
  }
  
  return fmt;
}

/**
 * 相对时间格式化
 * @param {string|Date} date - 日期对象或日期字符串
 * @returns {string} - 相对时间，如"刚刚"、"5分钟前"、"2小时前"等
 */
export function formatRelativeTime(date) {
  if (!date) return '';
  
  if (typeof date === 'string') {
    date = new Date(date);
  }
  
  const now = new Date();
  const diff = now.getTime() - date.getTime(); // 时间差(毫秒)
  
  if (diff < 0) {
    return formatDate(date);
  }
  
  const minute = 60 * 1000; // 1分钟
  const hour = 60 * minute; // 1小时
  const day = 24 * hour; // 1天
  const week = 7 * day; // 1周
  const month = 30 * day; // 1月(近似)
  
  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前';
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前';
  } else if (diff < week) {
    return Math.floor(diff / day) + '天前';
  } else if (diff < month) {
    return Math.floor(diff / week) + '周前';
  } else {
    return formatDate(date, 'yyyy-MM-dd');
  }
} 