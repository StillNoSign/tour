package com.manager.tour.dao;

import com.manager.entry.tour.ImageShow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ImageShowMapper {
    int deleteByPrimaryKey(String sc020102Id);

    int insert(ImageShow record);

    int insertSelective(ImageShow record);

    ImageShow selectByPrimaryKey(String sc020102Id);

    int updateByPrimaryKeySelective(ImageShow record);

    int updateByPrimaryKey(ImageShow record);

    @Select("select count(*) from SC020102 where PIC_SOURCE = #{picSource}")
    int selectCount(@Param("picSource") String picSource);

    int insertAll(@Param("imageShows")List<ImageShow> imageShows);

    int deleteAll(@Param("sc0201Id") String sc0201Id, @Param("ids") List<String> ids, @Param("ss01Id") String userUid);

    int deleteAllByContent(@Param("ids") List<String> ids, @Param("ss01Id") String userUid);
}
