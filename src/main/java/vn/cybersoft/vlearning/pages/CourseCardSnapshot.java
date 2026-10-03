package vn.cybersoft.vlearning.pages;

public record CourseCardSnapshot(
        String name,
        String href,
        int cardIndex,
        boolean hasSubCard,
        ParentContent parent,
        SubCardContent subCard
) {

    public record ParentContent(
            String courseName,
            String instructorName,
            String instructorAvatarSrc,
            String hours,
            String weeks,
            String level,

            // TC4 - main visible Course card
            String courseImageSrc,
            boolean courseImageDisplayed,
            int courseImageNaturalWidth,

            boolean instructorAvatarDisplayed,
            int instructorAvatarNaturalWidth,

            String originalPrice,
            String discountedPrice,
            String originalPriceDecoration,

            String ratingScore,
            String reviewCount
    ) {
    }

    public record SubCardContent(
            String courseName,
            String instructorName,
            String instructorAvatarSrc,
            String description,
            String hours,
            String weeks,
            String level,
            String detailButtonText
    ) {
    }
}