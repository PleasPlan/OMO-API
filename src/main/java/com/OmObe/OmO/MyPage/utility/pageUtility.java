package com.OmObe.OmO.MyPage.utility;

import com.OmObe.OmO.Place.entity.PlaceLike;
import com.OmObe.OmO.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;

public class pageUtility<T> {
    public Slice<T> convertToSlice(Page<T> page){
        return new SliceImpl<>(page.getContent(), page.getPageable(), page.hasNext());
    }

    public Specification<T> withMember(Member member){
        return (Specification<T>) ((root, query, builder) ->
                builder.equal(root.get("member"),member));
    }
    public Specification<T> withMemberAndType(Member member,String type){
        return (Specification<T>) ((root, query, builder) ->
                builder.and(
                builder.equal(root.get("member"),member),
                builder.equal(root.get("type"),type))
        );
    }
}
