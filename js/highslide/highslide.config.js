/**
*	Site-specific configuration settings for Highslide JS
*/
hs.graphicsDir = 'js/highslide/graphics/';
hs.dimmingOpacity = 0.65;
hs.fadeInOut = true;
hs.align = 'center';
hs.captionEval = 'this.a.title';

hs.registerOverlay({
	html: '<div class="closebutton" onclick="return hs.close(this)" title="Close"></div>',
	position: 'top right',
	fade: 2 // fading the semi-transparent overlay looks bad in IE
});

if (useGallery) {
	hs.wrapperClassName = 'borderless';

	// Add the slideshow controller
	hs.addSlideshow({
		slideshowGroup: 'group1',
		interval: 5000,
		repeat: false,
		useControls: true,
		fixedControls: 'fit',
		overlayOptions: {
			opacity: 0.75,
			position: 'bottom center',
			offsetX: 0,
			offsetY: -15,
			hideOnMouseOut: true
		}
	});
	
	// gallery config object
	var config1 = {
		slideshowGroup: 'group1',
		thumbnailId: 'thumb1',
		numberPosition: 'caption',
		transitions: ['expand', 'crossfade']
	};

}

else {
	//hs.outlineType = 'rounded-white';//'custom';
	hs.wrapperClassName = 'borderless';
}
