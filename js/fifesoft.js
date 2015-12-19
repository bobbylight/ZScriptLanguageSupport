// Make sections expandable
$(function() {
   $('.sectionHeader').click(function() {
      var img = $(this).find('img');
      var image = img.attr('src');
      image = image=='/img/section_expanded.png' ? '/img/section_collapsed.png' :
         '/img/section_expanded.png';
      img.attr('src', image);
      $(this).next('.sectionContent').stop(false, true).slideToggle();
   });
   $('.sectionHeader').mousedown(function() {
      return false; // Prevents selection on fast clicking
   });
});
