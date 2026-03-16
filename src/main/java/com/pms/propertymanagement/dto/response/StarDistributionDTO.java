package com.pms.propertymanagement.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StarDistributionDTO {
    private long oneStar;
    private long twoStars;
    private long threeStars;
    private long fourStars;
    private long fiveStars;
    private long totalReviews;

    public int getOneStarPercentage() { return calculatePercentage(oneStar); }
    public int getTwoStarPercentage() { return calculatePercentage(twoStars); }
    public int getThreeStarPercentage() { return calculatePercentage(threeStars); }
    public int getFourStarPercentage() { return calculatePercentage(fourStars); }
    public int getFiveStarPercentage() { return calculatePercentage(fiveStars); }

    private int calculatePercentage(long count) {
        if (totalReviews == 0) return 0;
        return (int) ((count * 100.0) / totalReviews);
    }
}
