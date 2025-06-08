import {http} from "@/utils/http.js";


//获取非遗项目列表
export const getProjectList = async (params) => {
    const { current, size, keyword, levelId, categoryId, regionId } = params;
    return http.get("/web/ich/getProject", {
        current,
        size,
        keyword,
        levelId,
        categoryId,
        regionId
    });
}

//获取非遗项目详情
export const getProjectDetail = async (id) => {
    return http.get(`/web/ich/getProjectDetail/${id}`);
}

//获取地区列表
export const getRegionList = async () => {
    return http.get("/web/ich/getRegion");
}

