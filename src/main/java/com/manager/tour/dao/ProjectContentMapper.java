package com.manager.tour.dao;

import com.manager.entry.tour.ProjectContent;
import com.manager.entry.tour.ProjectDocumentNum;
import com.manager.entry.tour.ProjectScore;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProjectContentMapper {
    int deleteByPrimaryKey(String sc0201Id);

    int insert(ProjectContent record);

    int insertSelective(ProjectContent record);

    ProjectContent selectByPrimaryKey(String sc0201Id);

    int updateByPrimaryKeySelective(ProjectContent record);

    int updateByPrimaryKey(ProjectContent record);

    /**
     * 批量新增
     * @param projectContents
     * @return
     */
    int insertAll(@Param("projectContents") List<ProjectContent> projectContents);

    ProjectContent selectDetail(@Param("sc0201Id") String sc0201Id);

    int deleteAll(@Param("projectNo") String projectNo, @Param("ids") List<String> ids, @Param("ss01Id") String userUid);

    List<String> selectDeleteId(@Param("projectNo") String projectNo, @Param("ids") List<String> ids);

    List<ProjectContent> selectProjectContentLevel(@Param("projectNo") String projectNo, @Param("code") String code, @Param("level") String level);

    /**
     * 根据父节点superContentNo 查询所有子节点
     * @param projectNo
     * @param code
     * @return
     */
    List<ProjectContent> selectProjectContentAll(@Param("projectNo") String projectNo, @Param("code") String code);

    List<ProjectScore> selectProjectScoreStatistics(@Param("projectNo") String projectNo);

    List<ProjectDocumentNum> selectProjectDocumentStatistics(@Param("projectNo") String projectNo);
}
