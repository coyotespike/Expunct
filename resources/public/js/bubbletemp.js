var vis4 = function() {}; 

vis4.log = function() {
	try {
		if (window.console !== null) console.log.apply(this, arguments);
	} catch (e) {};
};

vis4.DelayedTask = function(/* delay, scope, func, args */) {
	
	var me = this;
	
	me.init = function(args) {
		var me = this, taskArgs = [];	
		for (var i in args) {
			if (i > 2) taskArgs.push(args[i]);
		}
		me.func = args[2];
		me.scope = args[1];
		me.args = taskArgs;
		me.running = true;
		me.timer = window.setTimeout(this.run.bind(me), args[0]);
	};
	
	me.run = function() {
		var me = this;
		me.func.apply(me.scope, me.args);
		me.running = false;
	};
	
	me.cancel = function() {
		vis4.log('canceling timer', this);
		window.clearTimeout(this.timer);
		this.running = false;
	};
	
	me.init(arguments);
};


var vis4color = function(mode) {
	
	this.h = 0; this.s = 0.5; this.l = 0.8; this.v = 1; this.i = 1; this.r = 255; this.g = 0; this.b = 0; 
	this.x = "#FF0000"; this.u = 0; this.br = 1; this.K = 1/180*Math.PI;
	if (mode == 'hsi' || mode == 'hsl' || mode == 'hsb' || mode == 'hsv') this.mode = mode;

	this.log = function(s) {
		if (window.console !== null) console.log(s);
	};
	
	this.cos = function(d) {
		return Math.cos(d*this.K);
	};

	this.trim = function(value) {
		return Math.max(0, Math.min(1, value));
	};

	this.setMode = function(mode) {
		if (mode != 'hsv' && mode != 'hsi' && mode != 'hsl' && mode != 'hsb') {
			this.log("unknown color mode "+mode);
		}
		this.mode = mode;
		// recalc hsx-color
		this.rgb2hsx();	
	};

	this.setHexColor = function(hex) {
		if (hex.charAt(0) != "#") hex = "#"+hex;
		if (hex.length == 4) hex = "#"+hex.charAt(1)+hex.charAt(1)+hex.charAt(2)+hex.charAt(2)+hex.charAt(3)+hex.charAt(3);
		if (hex.length != 7) this.log("invalid hex color");
		this.x = hex;
		this.hex2int();
		this.int2rgb();
		this.rgb2hsx();
	};

	this.setRGBColor = function(r,g,b) {
		this.r = r; this.b = b; this.g = g;
		this.rgb2int();
		this.int2hex();
		this.rgb2hsx();
	};

	this.setHSVColor = function(h,s,v) {
		if (this.mode != 'hsv') this.mode = 'hsv';
		this.h = h; this.s = this.trim(s); this.v = this.trim(v);
		this.hsv2rgb();
		this.rgb2int();
		this.int2hex();
	};

	this.setHSLColor = function(h,s,l) {
		if (this.mode != 'hsl') this.mode = 'hsl';
		this.h = h; this.s = this.trim(s); this.l = this.trim(l);
		this.hsl2rgb();
		this.rgb2int();
		this.int2hex();
	};

	this.setHSBColor = function(h,s,b) {
		if (this.mode != 'hsb') this.mode = 'hsb';
		this.h = h; this.s = this.trim(s); this.b = this.trim(b);
		this.hsb2rgb();
		this.rgb2int();
		this.int2hex();
	};

	this.setHSIColor = function(h,s,i) {
		if (this.mode != 'hsi') this.mode = 'hsi';
		this.h = h; this.s = this.trim(s); this.i = this.trim(i);
		this.hsi2rgb();
		this.rgb2int();
		this.int2hex();
	};

	// private methods

	this.onChange = function() { };

	this.rgb2int = function() {
		this.u = this.r << 16 | this.g << 8 | this.b;
		this.onChange();
	};

	this.int2rgb = function() {
		this.r = this.u >> 16;
		this.g = this.u >> 8 & 0xFF;
		this.b = this.u & 0xFF;	
	};

	this.hex2int = function() {
		this.u = parseInt(this.x.substr(1), 16);
		this.onChange();
	};

	this.int2hex = function() {
		var str = "000000" + this.u.toString(16).toUpperCase();
		this.x = "#" + str.substr(str.length - 6);
		this.onChange();
	};

	this.int2rgb = function() {
		this.r = this.u >> 16;
		this.g = this.u >> 8 & 0xFF;
		this.b = this.u & 0xFF;	
	};

	this.hsx2rgb = function() {
		switch (this.mode) {
			case 'hsv': this.hsv2rgb(); break;
			case 'hsi': this.hsi2rgb(); break;
			case 'hsl': this.hsl2rgb(); break;
			case 'hsb': this.hsb2rgb(); break;
		}
	};

	this.rgb2hsx = function() {
		switch (this.mode) {
			case 'hsv': this.rgb2hsv(); break;
			case 'hsi': this.rgb2hsi(); break;
			case 'hsl': this.rgb2hsl(); break;
			case 'hsb': this.rgb2hsb(); break;
		}
	};

	this.hue = function(h) {
		this._evaluate(h, "h");
		this.hsx2rgb();
		this.rgb2int();
		this.int2hex();
		return this;
	};

	this.saturation = function(s) {
		this._evaluate(s, "s"); 
		this.hsx2rgb();
		this.rgb2int();
		this.int2hex();
		return this;
	};

	this.lightness = function(l) {
		if (this.mode != "hsl") { this.log("WARNING: lightness property not available in "+this.mode+" mode"); return; }
		this._evaluate(l, "l"); 
		this.hsx2rgb();
		this.rgb2int();
		this.int2hex();
		return this;
	};

	this.brightness = function(br) {
		if (this.mode != "hsb") { this.log("WARNING: brightness property not available in "+this.mode+" mode"); return; }
		this._evaluate(br, "br");
		this.hsx2rgb();
		this.rgb2int();
		this.int2hex();
		return this;
	};

	this.value = function(v) {
		if (this.mode != "hsv") { this.log("WARNING: value property not available in "+this.mode+" mode"); return; }
		this._evaluate(v, "v");
		this.hsx2rgb();
		this.rgb2int();
		this.int2hex();
		return this;
	};

	this.intensity = function(i) {
		if (this.mode != "hsi") { this.log("WARNING: intensity property not available in "+this.mode+" mode"); return; }
		this._evaluate(i, "i"); 
		this.hsx2rgb();
		this.rgb2int();
		this.int2hex();
		return this;
	};

	this._evaluate = function(val, propName) {
		if (typeof(val) == "string") {
			if (val.charAt(0) == "+" && !isNaN(val.substr(1))) {
				this[propName] = Number(this[propName]) + Number(val.substr(1));
			} else if (val.charAt(0) == "-" && !isNaN(val.substr(1))) {
				this[propName] = this[propName] - Number(val.substr(1));
			} if (val.charAt(0) == "*" && !isNaN(val.substr(1))) {
				this[propName] = this[propName] * Number(val.substr(1));
			} else if (val.charAt(0) == "/" && !isNaN(val.substr(1))) {
				this[propName] = this[propName] / Number(val.substr(1));
			} 
		} else if (!isNaN(val)) {
			this[propName] = Number(val);
		}
	};

	this.rgb = function() { return [this.r,this.g,this.b]; };
	this.hsl = function() { return [this.h,this.s,this.l]; };

	// hsv magic

	this.rgb2hsv = function() {
		var min = Math.min(Math.min(this.r, this.g), this.b),
			max = Math.max(Math.max(this.r, this.g), this.b),
			delta = max - min;
		
		this.v = max/255;
		this.s = delta / max;
		if (this.s === 0) {
			this.h = undefined;
		} else {
			if (this.r == max) this.h = (this.g - this.b) / delta;
			if (this.g == max) this.h = 2+(this.b - this.r) / delta;
			if (this.b == max) this.h = 4+(this.r - this.g) / delta;
			this.h *= 60;
			if (this.h < 0) this.h += 360;
		}
	};

	this.hsv2rgb = function() {
		var h = this.h, s = this.s, _rgb = this._rgb, v = this.v*255, i, f, p, q, t;
		
		if (this.s === 0 && isNaN(h)) {
			this.r = this.g = this.b = v;
		} else {
			if (h == 360) h = 0;
			h /= 60;
			i = Math.floor(h);
			f = h - i;
			p = v * (1 - s);
			q = v * (1 - s * f);
			t = v * (1 - s * (1 - f));
			
			switch (i) {
				case 0: _rgb(v, t, p); break;
				case 1: _rgb(q, v, p); break;
				case 2: _rgb(p, v, t); break;
				case 3: _rgb(p, q, v); break;
				case 4: _rgb(t, p, v); break;
				case 5: _rgb(v, p, q); 
			}
		}			
	};

	this._rgb = function(r,g,b) {
		this.r = r; this.g = g; this.b = b;
	};
	// hsl magic

	this.rgb2hsl = function() {
		var r = this.r / 255, 
			g = this.g / 255, 
			b = this.b / 255,
			min = Math.min(Math.min(r, g), b),
			max = Math.max(Math.max(r, g), b);
		
		this.l = (max + min) / 2;
		if (max == min) {
			this.s = 0;
			this.h = undefined;
		} else {
			if (this.l < 0.5) {
				this.s = (max - min) / (max + min);
			} else {
				this.s = (max - min) / (2 - max - min);
			}
		}
		if (r == max) this.h = (g - b) / (max - min);
		else if (g == max) this.h = 2 + (b - r) / (max - min);
		else if (b == max) this.h = 4 + (r - g) / (max - min);
		
		this.h *= 60;
		if (this.h < 0) this.h += 360;
	};

	this.hsl2rgb = function() {
		if (this.s === 0) {
			this.r = this.g = this.b = this.l*255;
		} else {
			var t1, t2, t3 = [0,0,0], c = [0,0,0];
			if (this.l < 0.5) {
				t2 = this.l * (1 + this.s);
			} else {
				t2 = this.l + this.s - this.l * this.s;
			}
			t1 = 2 * this.l - t2;
			var h = this.h / 360;
			t3[0] = h + 1 / 3;
			t3[1] = h;
			t3[2] = h - 1 / 3;
			for (var i = 0; i < 3; i++) {
				if (t3[i] < 0) t3[i] += 1;
				if (t3[i] > 1) t3[i] -= 1;
				
				if (6 * t3[i] < 1) c[i] = t1 + (t2 - t1) * 6 * t3[i];
				else if (2 * t3[i] < 1) c[i] = t2;
				else if (3 * t3[i] < 2) c[i] = t1 + (t2 - t1) * ((2 / 3) - t3[i]) * 6;
				else c[i] = t1;
			}
			this.r = c[0] * 255;
			this.g = c[1] * 255;
			this.b = c[2] * 255;
		}
	};

	// hsb magic

	this.rgb2hsb = function() {
		this.rgb2hsl();
		this.br = this._rgbLuminance();
	};

	this._rgbLuminance = function() {
		return (0.2126 * this.r + 0.7152 * this.g + 0.0722 * this.b) / 255; 
	};

	this.hsb2rgb = function() {
		var treshold = 0.001;
		var l_min = 0, l_max = 1, l_est = 0.5;
		var current_brightness;
		
		// first try
		this.l = l_est;
		this.hsl2rgb();
		current_brightness = this._rgbLuminance();
		var trys = 0;
		
		while (Math.abs(current_brightness - this.br) > treshold && trys < 100) {
			
			if (current_brightness > this.br) {
				// too bright, next try darker
				l_max = l_est;					
			} else {
				// too dark, next try brighter
				l_min = l_est;
			}
			l_est = (l_min + l_max) / 2;
			this.l = l_est;
			this.hsl2rgb();
			current_brightness = this._rgbLuminance();
			trys++;
		}
		this.br = current_brightness;
	};

	// hsi magic

	this.rgb2hsi = function() { // http://fourier.eng.hmc.edu/e161/lectures/colorprocessing/node3.html
		var min, r = this.r, g = this.g, b = this.b,
			max = Math.max(Math.max(r, g), b),
			sum = r + g + b,
			delta = max - min;
		
		r = r / sum;
		g = g / sum;
		b = b / sum;
		
		min = Math.min(Math.min(r, g), b);
		//trace('rgb = ',r,g,b,' min = ' + min);
		
		this.i = (r + g + b) / 765;			
		this.h = this.acos((r - 0.5*g - 0.5*b) / Math.sqrt( (r - g) * (r - g) + (r - b) * (g - b)) );			
		this.s = 1 - 3 * min;
		
		if (b > g) this.h = 360 - this.h;
	};	
		
	this.hsi2rgb = function() { // http://fourier.eng.hmc.edu/e161/lectures/colorprocessing/node4.html
		var h = this.h,i=this.i,s=this.s, r, b, g, cos = this.cos;
		
		if (h <= 120) {
			b = (1 - s) / 3;
			r = (1 + (s * cos(h)) / cos(60 - h)) / 3;
			g = 1 - (b + r);
		} else if (h <= 240) {
			h -= 120;
			r = (1 - s) / 3;
			g = (1 + (s * cos(h)) / cos(60 - h)) / 3;
			b = 1 - (r + g);
		} else {
			h -= 240;
			g = (1 - s) / 3;
			b = (1 + (s * cos(h)) / cos(60 - h)) / 3;
			r = 1 - (g + b);
		}
		r = Math.min(255, r*i*3*255);
		g = Math.min(255, g*i*3*255);
		b = Math.min(255, b*i*3*255);			
	};
};

// static constructors
			
vis4color.fromHex = function(color, mode) {
	if (mode == null) mode = 'hsl';
	var c = new vis4color(mode);
	c.setHexColor(color);
	return c;
};


/*This section uses in vis4: vis4, vis4color, vis4log, vis4DelayedTask */
var BubbleTree = function(config, onHover, onUnHover) {

	var me = this;

	me.version = "2.0.2";

	me.$container = $(config.container);

	me.config = $.extend({
		// this is where we look for the icons
		rootPath: '',
		// show full labels inside bubbles with min radius of 40px
		minRadiusLabels: 40,
		// just show the amounts inside bubbles with min radius of 20px
		minRadiusAmounts: 20,
		// hide labels at all for bubbles with min radius of 0 (deactivated by def)
		minRadiusHideLabels: 0,
		// trim labels after 50 characters
		cutLabelsAt: 50
	}, config);

	/*
	 * this function is called when the user hovers a bubble
	 */
	//me.onHover = onHover;

	//me.onUnHover = onUnHover;
	me.tooltip = config.tooltipCallback ? config.tooltipCallback : function() {};
	if (config.tooltip) me.tooltip = config.tooltip;

	/*
	 * stylesheet JSON that contains colors and icons for the bubbles
	 */
	me.style = config.bubbleStyles;

	me.ns = BubbleTree;

	/*
	 * hashmap of all nodes by url token
	 */
	me.nodesByUrlToken = {};

	/*
	 * flat array of all nodes
	 */
	me.nodeList = [];

	me.iconsByUrlToken = {};

	me.globalNodeCounter = 0;

	me.displayObjects = [];

	me.bubbleScale = 1;

	me.globRotation = 0;

	me.currentYear = config.initYear;

	me.currentCenter = undefined;

	me.currentTransition = undefined;

	me.baseUrl = '';

	/*
	 * @public loadData
	 */
	me.loadData = function(url) {
		$.ajax({
			url: url,
			dataType: 'json',
			success: this.setData.bind(this)
		});
	};

	/*
	 * is either called directly or by $.ajax when data json file is loaded
	 */
	me.setData = function(data) {
		var me = this;
		if (!data) data = me.config.data; // IE fix
		me.initData(data);
		me.initPaper();
		me.initBubbles();
		me.initTween();
		me.initHistory();
	};

	/*
	 * initializes the data tree, adds links to parent node for easier traversal etc
	 */
	me.initData = function(root) {
		var me = this;
		root.level = 0;
		me.preprocessData(root);
		me.traverse(root, 0);
		me.treeRoot = root;
	};

	me.preprocessData = function(root) {
		var me = this, maxNodes = me.config.maxNodesPerLevel;
		if (maxNodes) {
			if (maxNodes < root.children.length) {
				// take the smallest nodes
				// sort children
				var tmp = me.sortChildren(root.children);
				tmp.reverse();
				var keep = [], move = [], moveAmount = 0, breakdown;
				for (var i in root.children) {
					if (i < maxNodes) {
						keep.push(root.children[i]);
					} else {
						move.push(root.children[i]);
						moveAmount += Math.max(0, root.children[i].amount);
					}
				}
				root.children = keep;
				root.children.push({
					'label': 'More',
					'name': 'more',
					'amount': moveAmount,
					'children': move,
					'breakdown': breakdown
				});
			}
		}
	};

	/*
	 * used for recursive tree traversal
	 */
	me.traverse = function(node, index) {
		var c, child, pc, me = this, urlTokenSource, styles = me.config.bubbleStyles;

		//if (node.amount <= 0) return;

		if (!node.children) node.children = [];

		// store node in flat node list
		me.nodeList.push(node);

		node.famount = me.ns.Utils.formatNumber(node.amount);
		if (node.parent) node.level = node.parent.level + 1;

		if (me.config.clearColors === true) node.color = false;

		if (styles) {

			var props = ['color', 'shortLabel', 'icon'];

			$.each(props, function (p, prop) {
				if (styles.hasOwnProperty('id') && styles.id.hasOwnProperty(node.id) && styles.id[node.id].hasOwnProperty(prop)) {
					// use color by id
					node[prop] = styles.id[node.id][prop];
				} else if (node.hasOwnProperty('name') && styles.hasOwnProperty('name') && styles.name.hasOwnProperty(node.name) && styles.name[node.name].hasOwnProperty(prop)) {
					// use color by id
					node[prop] = styles.name[node.name][prop];
				} else if (node.hasOwnProperty('taxonomy') && styles.hasOwnProperty(node.taxonomy) && styles[node.taxonomy].hasOwnProperty(node.name) && styles[node.taxonomy][node.name].hasOwnProperty(prop)) {
					node[prop] = styles[node.taxonomy][node.name][prop];
				}
			});
		}

		if (!node.color) {
			// use color from parent node if no other match available
			if (node.level > 0) node.color = node.parent.color;
			else node.color = '#999999';
		}
		// lighten up the color if there are no children
		if (node.children.length < 2 && node.color) {
			node.color = vis4color.fromHex(node.color).saturation('*.86').x;
		}

		if (node.level > 0) {
			pc = node.parent.children;
			if (pc.length > 1) {
				node.left = pc[(index-1+pc.length) % pc.length];
				node.right = pc[(Number(index)+1) % pc.length];
				if (node.right == node.left) node.right = undefined;
			}
		}
		if (node.label !== undefined && node.label !== "") {
			urlTokenSource = node.label;
		} else if (node.token !== undefined && node.token !== "") {
			urlTokenSource = node.token;
		} else {
			urlTokenSource = ''+me.globalNodeCounter;
		}

		me.globalNodeCounter++;

		node.urlToken = urlTokenSource.toLowerCase().replace(/\W/g, "-");
		while (me.nodesByUrlToken.hasOwnProperty(node.urlToken)) {
			node.urlToken += '-';
		}
		me.nodesByUrlToken[node.urlToken] = node;
		node.maxChildAmount = 0;

		// sort children
		node.children = me.sortChildren(node.children, true, me.config.sortBy);

		$.each(node.children, function(c, child) {
			child.parent = node;
			node.maxChildAmount = Math.max(node.maxChildAmount, child.amount);
			me.traverse(child, c);
		});

		if (node.breakdowns) {
			node.breakdownsByName = {};
			$.each(node.breakdowns, function (c,bd) {
				bd.famount = me.ns.Utils.formatNumber(bd.amount);
				if (bd.name) node.breakdownsByName[bd.name] = bd;
			});
		}
	};

	me.sortChildren = function(children, alternate, sortBy) {
		var tmp = [], odd = true;
		if (sortBy == 'label') {
			sortBy = me.compareLabels;
			alternate = false;
		} else sortBy = me.compareAmounts;

		children.sort(sortBy);
		if (alternate) {
			while (children.length > 0) {
				tmp.push(odd ? children.pop() : children.shift());
				odd = !odd;
			}
			return tmp;
		} else {
			return children;
		}
	};

	/*
	 * compares two items by amount
	 */
	me.compareAmounts = function(a, b) {
		if (a.amount > b.amount) return 1;
		if (a.amount == b.amount) return 0;
		return -1;
	};

	/*
	 * compares to item by label
	 */
	me.compareLabels = function(a, b) {
		if (a.label > b.label) return 1;
		if (a.label == b.label) return 0;
		return -1;
	};

	/*
	 * initializes all that RaphaelJS stuff
	 */
	me.initPaper = function() {
		var me = this, $c = me.$container, rt = me.treeRoot,
			w = $c.width(), h = $c.height(),
			paper = Raphael($c[0], w, h),
			maxRad = Math.min(w, h) * 0.5 - 40,
			base, Vector = me.ns.Vector,
			origin = new Vector(w * 0.5, h * 0.5); // center

		me.width = w;
		me.height = h;
		me.paper = paper;
		base = Math.pow((Math.pow(rt.amount, 0.6) + Math.pow(rt.maxChildAmount, 0.6)*2) / maxRad, 1.6666666667);
		me.a2radBase = me.ns.a2radBase = base;

		me.origin = origin;

		$(window).resize(me.onResize.bind(me));
	};

	me.onResize = function() {
		var me = this, $c = me.$container, w = $c.width(), h = $c.height(),
			maxRad = Math.min(w, h) * 0.5 - 40, base, rt = me.treeRoot, b, obj;
		me.paper.setSize(w, h);
		me.origin.x = w * 0.5;
		me.origin.y = h * 0.5;
		me.width = w;
		me.height = h;
		base = Math.pow((Math.pow(rt.amount, 0.6) + Math.pow(rt.maxChildAmount, 0.6)*2) / maxRad, 1.6666666667);
		me.a2radBase = me.ns.a2radBase = base;

		$.each(me.displayObjects, function(b, obj) {
			if (obj.className == "bubble") {
				obj.bubbleRad = me.ns.Utils.amount2rad(obj.node.amount);
			}
		});
		// vis4.log(me);
		if (me.currentCenter) {
			me.changeView(me.currentCenter.urlToken);
		}
	};

	/*
	 * initializes the Tweening engine
	 */
	me.initTween = function() {
		this.tweenTimer = setInterval(this.loop, 1000/120);
	};

	/*
	 * creates instances for all bubbles in the dataset. the bubbles will
	 * remain invisble until they enter the stage via changeView()
	 */
	me.initBubbles = function() {
		//vis4.log('initBubbles');
		var me = this, rt = me.treeRoot, i, icons = false, Bubbles = me.ns.Bubbles, bubbleClass;

		me.bubbleClasses = [];

		// defaults to plain bubble
		if (!me.config.hasOwnProperty('bubbleType')) me.config.bubbleType = ['plain'];
		// convert to array if neccessairy
		if (!$.isArray(me.config.bubbleType)) me.config.bubbleType = [me.config.bubbleType];

		if ($.isArray(me.config.bubbleType)) {
			$.each(me.config.bubbleType, function(i) {
				if (me.config.bubbleType[i] == 'icon') icons = true;
				me.bubbleClasses.push(me.getBubbleType(me.config.bubbleType[i]));
			});
		}

		var rootBubble = me.createBubble(rt, me.origin, 0, 0, rt.color);
		me.traverseBubbles(rootBubble);
	};

	/*
	 * returns the bubble class for a given bubble class id
	 * e.g. 'icon' > BubbleTree.Bubbles.Icon
	 */
	me.getBubbleType = function(id) {
		var me = this, Bubbles = me.ns.Bubbles;
		// choose one of them for the vis
		switch (id) {
			case 'pie': return Bubbles.Pies;
			case 'donut': return Bubbles.Donut;
			case 'multi': return Bubbles.Multi;
			case 'icon': return Bubbles.Icon;
			default: return Bubbles.Plain;
		}
	};

	/*
	 * iterates over the complete tree and creates a bubble for
	 * each node
	 */
	me.traverseBubbles = function(parentBubble) {
		var me = this, ring,
			a2rad = me.ns.Utils.amount2rad,
			i, c, children, childBubble, childRadSum = 0, oa = 0, da, ca, twopi = Math.PI * 2;
		children = parentBubble.node.children;

		// sum radii of all children
		$.each(children, function(i,c) {
			childRadSum += a2rad(c.amount);
		});

		if (children.length > 0) {
			// create ring
			ring = me.createRing(parentBubble.node, parentBubble.pos, 0, { stroke: '#888', 'stroke-dasharray': "-" });
		}

		$.each(children, function(i,c) {

			da = a2rad(c.amount) / childRadSum * twopi;
			ca = oa + da*0.5;

			if (isNaN(ca)) vis4.log(oa, da, c.amount, childRadSum, twopi);

			c.centerAngle = ca;

			childBubble = me.createBubble(c, parentBubble.pos, 0, ca, c.color);
			// für jedes kind einen bubble anlegen und mit dem parent verbinden
			oa += da;

			me.traverseBubbles(childBubble);
		});

	};


	/*
	 * creates a new bubble for a given node. the bubble type will be chosen
	 * by the level of the node
	 */
	me.createBubble = function(node, origin, rad, angle, color) {
		var me = this, ns = me.ns, i, b, bubble, classIndex = node.level;
		if (isNaN(classIndex)) classIndex = 0;
		classIndex = Math.min(classIndex, me.bubbleClasses.length-1);

		bubble = new me.bubbleClasses[classIndex](node, me, origin, rad, angle, color);
		me.displayObjects.push(bubble);
		return bubble;
	};

	me.createRing = function(node, origin, rad, attr) {
		var me = this, ns = me.ns, ring;
		ring = new ns.Ring(node, me, origin, rad, attr);
		me.displayObjects.push(ring);
		return ring;
	};

	/*
	 * is called every time the user changes the view
	 * each view is defined by the selected node (which is displayed
	 */
	me.changeView = function(token) {
		var me = this,
			paper = me.paper,
			maxRad = Math.min(me.width, me.height) * 0.35,
			ns = me.ns,
			utils = ns.Utils,
			o = me.origin,
			l1attr = { stroke: '#ccc', 'stroke-dasharray': "- " },
			l2attr = { stroke: '#ccc', 'stroke-dasharray': ". " },
			a2rad = utils.amount2rad,
			root = me.treeRoot,
			nodesByUrlToken = me.nodesByUrlToken,
			node = nodesByUrlToken.hasOwnProperty(token) ? nodesByUrlToken[token] : null,
			t = new ns.Layout(),
			bubble, tr, i, twopi = Math.PI * 2,
			getBubble = me.getBubble.bind(me), getRing = me.getRing.bind(me),
			unify = me.unifyAngle;

		if (node !== null) {

			// what do you we have to do here?
			// - find out the origin position
			// -

			var parent, grandpa, sibling, c, cn, rad1, rad2, rad, srad, sang, ring, tgtScale,
				radSum, leftTurn = false, rightTurn = false;



			// initially we will mark all bubbles and rings for hiding
			// get....() will set this flag to false
			for (i in me.displayObjects) me.displayObjects[i].hideFlag = true;


			if (node == root || node.parent == root && node.children.length < 2) {

				t.$(me).bubbleScale = 1.0;

				// move origin to center
				t.$(o).x = me.width * 0.5;
				t.$(o).y = me.height * 0.5;

				// make the root bubble visible
				parent = getBubble(root);

				//parent.childRotation = 0;

				if (node != root) {
					parent.childRotation = -node.centerAngle;
				}

				rad1 = a2rad(root.amount) + a2rad(root.maxChildAmount) + 20;

				ring = getRing(root);
				t.$(ring).rad = rad1;

				for (i in root.children) {
					cn = root.children[i];
					// adjust rad and angle for children
					bubble = getBubble(cn);
					t.$(bubble).angle = unify(cn.centerAngle + parent.childRotation);
					t.$(bubble).rad = rad1;
				}

			} else {

				// node is not the root node

				var origNode = node; // save the reference of the node..

				if (node.children.length < 2) { // ..because if it has no children..
					node = node.parent;         // ..we center on its parent
				}

				tgtScale = maxRad / (a2rad(node.amount) + a2rad(node.maxChildAmount)*2);
				t.$(me).bubbleScale = tgtScale;

				parent = getBubble(node);

				if (me.currentCenter && me.currentCenter == node.left) rightTurn = true;
				else if (me.currentCenter && me.currentCenter == node.right) leftTurn = true;

				var sa = me.shortestAngleTo;
				//if (leftTurn) sa = me.shortestLeftTurn;
				//if (rightTurn) sa = me.shortestRightTurn;

				t.$(parent).angle = sa(parent.angle, 0);

				// find the sum of all radii from node to root
				rad1 = (a2rad(node.amount) + a2rad(node.maxChildAmount)) * tgtScale + 20;

				ring = getRing(node);
				t.$(ring).rad = rad1;

				grandpa = getBubble(node.parent);
				grandpa.childRotation = -node.centerAngle;

				var maybeRoot = grandpa;

				while (maybeRoot && maybeRoot.node.parent) {
					maybeRoot = getBubble(maybeRoot.node.parent, true);
					t.$(maybeRoot).rad = 0;
				}

				t.$(grandpa).rad = 0;
				//
				var hw = me.width * 0.5;

				rad2 = 0 - Math.max(
					//hw *0.8 - tgtScale * (a2rad(node.parent.amount)+a2rad(node.amount)), // maximum visible part
					hw * 0.8 - tgtScale * (a2rad(node.parent.amount) + a2rad(Math.max(node.amount*1.15 + node.maxChildAmount*1.15, node.left.amount * 0.85, node.right.amount * 0.85))),
					tgtScale*a2rad(node.parent.amount)*-1 + hw*0.15 // minimum visible part
				) + hw;

				//vis4.log('rad (parent) = '+rad2,'   rad (center) = ',rad1);

				if (node.left && node.right) {
					var maxSiblSize = tgtScale * a2rad(Math.max(node.left.amount, node.right.amount));
				}

				//rad2 = hw - (tgtScale*a2rad(node.parent.amount)*-1+ hw*0.15);

				radSum = rad1 + rad2;

				t.$(o).x = me.width * 0.5 - rad2 - (node != origNode ? rad1 * 0.35: 0);
				t.$(o).y = me.height * 0.5;

				//vis4.log('o.x = '+o.x,'    t.$(o).x = '+t.$(o).x);

				new vis4.DelayedTask(1500, vis4, vis4.log, o, grandpa.pos);

				rad2 += me.width * 0.1;

				ring = getRing(node.parent);
				t.$(ring).rad = rad2;

				t.$(parent).rad = rad2;

				var ao = 0-(node != origNode ? origNode.centerAngle + parent.childRotation: 0);
				// children
				for (i in node.children) {
					cn = node.children[i];
					// adjust rad and angle for children
					bubble = getBubble(cn);
					t.$(bubble).angle = me.shortestAngleTo(bubble.angle, cn.centerAngle + parent.childRotation + ao);
					t.$(bubble).rad = rad1;
				}

				// left and right sibling

				var siblCut = me.height * 0.07;

				if (node.left) {
					sibling = node.left;
					srad = a2rad(sibling.amount)*tgtScale;
					sang = twopi - Math.asin((me.paper.height*0.5 + srad - siblCut) / rad2);

					bubble = getBubble(sibling);
					t.$(bubble).rad = rad2;
					t.$(bubble).angle = sa(bubble.angle, sang);
				}
				if (node.right) {
					sibling = node.right;
					srad = a2rad(sibling.amount)*tgtScale;
					sang = Math.asin((me.paper.height*0.5 + srad - siblCut) / rad2);

					bubble = getBubble(sibling);
					t.$(bubble).rad = rad2;
					t.$(bubble).angle = sa(bubble.angle, sang);
				}

				node = origNode;
			}

			// now we're going to check all hides and shows
			for (i in me.displayObjects) {
				var obj = me.displayObjects[i];
				if (obj.hideFlag && obj.visible) {
					// bubble is on stage but shouldn't
					t.$(obj).alpha = 0; // let it disappear
					if (obj.className == "bubble" && obj.node.level > 1) t.$(obj).rad = 0; // move to center
					//else t.$(obj).rad =
					t.hide(obj); // remove from stage afterwards
				} else if (!obj.hideFlag) {
					// bubble is not on stage but should
					t.$(obj).alpha = 1;
					if (!obj.visible) {
						obj.alpha = 0;
						t.show(obj);
					}
				}
			}

			tr = new ns.Transitioner($.browser.msie || me.currentCenter == node ? 0 : 1000);
			tr.changeLayout(t);
			me.currentTransition = tr;
			if (!me.currentCenter && $.isFunction(me.config.firstNodeCallback)) {
				me.config.firstNodeCallback(node);
			}
			me.currentCenter = node;
			// vis4.log('currentNode = '+me.currentCenter);

		} else {
			utils.log('node '+token+' not found');
		}
		// step1:

		// step2:
	};

	me.unifyAngle = function(a) {
		var pi = Math.PI, twopi = pi * 2;
		while (a >= twopi) a -= twopi;
		while (a < 0) a += twopi;
		return a;
	};

	me.shortestAngle = function(f, t) {
		var deg = function(a) { return Math.round(a/Math.PI*180)+''; };
		var pi = Math.PI, twopi = pi * 2, unify= me.unifyAngle;
		f = unify(f);
		t = unify(t);
		var sa = t - f;
		if (sa > pi) sa -= twopi;
		if (sa < -pi) sa += twopi;

		return sa;
	};

	me.shortestAngleTo = function(f, t) {
		return f+me.shortestAngle(f, t);
	};

	me.shortestLeftTurn = function(f, t) {
		var sa = me.shortestAngle(f, t);
		if (sa > 0) sa = sa - Math.PI*2;
		return f+sa;
	};

	me.shortestRightTurn = function(f, t) {
		var sa = me.shortestAngle(f, t);
		if (sa < 0) sa = Math.PI*2 + sa;
		return f+sa;
	};


	/*
	 * returns the instance of a bubble for a given node
	 */
	me.getBubble = function(node, keepHidden) {
		return this.getDisplayObject('bubble', node, keepHidden);
	};

	/*
	 *
	 */
	me.getRing = function(node) {
		return this.getDisplayObject('ring', node);
	};

	me.getDisplayObject = function(className, node, keepHidden) {
		var me = this, i, o;
		for (i in me.displayObjects) {
			o = me.displayObjects[i];
			if (o.className != className) continue;
			if (o.node == node) {
				if (!keepHidden) o.hideFlag = false;
				return o;
			}
		}
		vis4.log(className+' not found for node', node);
	};

	/*
	me.createRing = function(t, origin, rad, attr) {
		var me = this, ns = me.ns,
			ring = new ns.Ring(me, origin, attr, rad);
		ring.toBack();
		me.rings.push(ring);
		t.$(ring).rad = rad;
		return ring;
	};
	*/

	me.initHistory = function() {
		$.history.init(me.urlChanged.bind(me), { unescape: ",/" });
	};

	me.freshUrl = '';

	/*
	 * callback for every url change, either initiated by user or
	 * by this class itself
	 */
	me.urlChanged = function(hash) {
		var me = this, tr = me.currentTransition;

		if (!me.freshUrl) {
			// setting an url for the very first time
			if (hash.indexOf('/~/')) {
				me.baseUrl = hash.substr(0, hash.indexOf('/~/'));
			}
		}
		me.freshUrl = hash;

		if (tr && tr.running) {
			vis4.log('transition is running at the moment, adding listener');
			tr.onComplete(me.changeUrl.bind(me));
		} else {
			me.changeUrl();
		}
	};

	/*
	 * this function initiate the action which follows the url change
	 */
	me.changeUrl = function() {
		var me = this, parts = me.freshUrl.split('/'), token = parts[parts.length-1], url;

		// var urlParts = me.freshUrl.split('/~/');
		if (me.freshUrl === "") me.navigateTo(me.treeRoot);

		if (me.nodesByUrlToken.hasOwnProperty(token)) {
			url = me.getUrlForNode(me.nodesByUrlToken[token]);
			if (me.freshUrl != url) {
				// node found but url not perfect
				$.history.load(url);
			} else {
				me.navigateTo(me.nodesByUrlToken[token], true);
			}
		} else {
			me.navigateTo(me.treeRoot);
		}
	};

	me.navigateTo = function(node, fromUrlChange) {
		// vis4.log('bc.navigateTo(',node,',',fromUrlChange,')');
		var me = this;
		if (fromUrlChange) me.changeView(node.urlToken);
		else $.history.load(me.getUrlForNode(node));
		//
		$('.label, .label2', me.$container).removeClass('current');
		$('.label2.'+node.id, me.$container).addClass('current');
		$('.label.'+node.id, me.$container).addClass('current');
	};

	/*
	 * creates a valid url for a given node, e.g. /2010/health/medical-supplies
	 */
	me.getUrlForNode = function(node) {
		var parts = [];
		parts.push(node.urlToken);
		while (node.parent) {
			parts.push(node.parent.urlToken);
			node = node.parent;
		}
		parts.reverse();
		return me.baseUrl+'/~/'+parts.join('/');
	};

	me.onNodeClick = function(node) {
		if ($.isFunction(me.config.nodeClickCallback)) {
			me.config.nodeClickCallback(node);
		}
	};

	// removes all nodes
	me.clean = function() {
		var me = this, i;
		$('.label').remove();
		/*for (i in me.displayObjects) {
			try {
				if ($.isFunction(me.displayObjects[i].hide)) me.displayObjects[i].hide();
			} catch (e) {

			}
		}*/
	};

	this.loop = function() {
		TWEEN.update();
	};

	if (!me.config.hasOwnProperty('data')) {
		throw new Error('no data');
	}

	if (typeof me.config.data == "string") {
		// use the given js object
		me.loadData();
	} else {
		// load local tree json file
		new vis4.DelayedTask(1000, me, me.setData, me.config.data);
	}
};
BubbleTree.MouseEventGroup = function(target, members) {
	
	var me = this;
	me.target = target; // e.g. instance of a bubble
	me.members = members; // e.g. raphael nodes or html elements
	
	/*
	 * public interface for setting click handlers
	 */
	me.click = function(callback) {
		var me = this, members = me.members, i, mem;
		me.clickCallback = callback;
		for (i in members) {
			mem = members[i];
			$(mem).click(me.handleClick.bind(me));
		}
	};
	
	me.handleClick = function(evt) {
		var me = this;
		me.clickCallback({ target: me.target, origEvent: evt, mouseEventGroup: me });
	};
	
	/*
	 *
	 */
	me.hover = function(callback) {
		var me = this, members = me.members, i, mem;
		me.hoverCallback = callback;
		for (i in members) {
			mem = members[i];
			$(mem).hover(me.handleMemberHover.bind(me), me.handleMemberUnHover.bind(me));
		}
	};
	
	/*
	 * public interface for setting unhover callback
	 */
	me.unhover = function(callback) {
		var me = this;
		me.unhoverCallback = callback;
	};
	
	/*
	 * stores wether the mouse currently hover over any
	 * object in our members list. this is used to check
	 * wether a occuring hover event is an actual hover
	 * event.
	 */
	me.wasHovering = false;
	me.mouseIsOver = false;
	
	me.handleMemberHover = function(evt) {
		var me = this;
		// since we don't know which event will receive first, the unhover of the member
		// the mouse is leaving or the hover of the member the mouse is entering, we will
		// delay the final check a bit
		new vis4.DelayedTask(25, me, me.handleMemberHoverDelayed, evt);	
		
	};

	/*
	 * will be called after all unhover events are processed
	 */
	me.handleMemberHoverDelayed = function(evt) {
		var me = this;
		// this will eventually override the false set by handleMemberUnHover a few
		// milliseconds ok. Exactly what we want!
		me.mouseIsOver = true;
				
		if (!me.wasHovering) {
			// the target is newly hovered
			
			me.wasHovering = true;
			if ($.isFunction(me.hoverCallback)) {
				me.hoverCallback({ target: me.target, origEvent: evt, mouseEventGroup: me });
			}
		} // else can be ignored, no news
	};
	

	me.handleMemberUnHover = function(evt) {
		var me = this;
		me.mouseIsOver = false;
		// we need to wait a bit to find out if this is a real unhover event
		// or just the change to another element in the member list
		// so we need to delay the final check a bit (let's say 30ms)
		new vis4.DelayedTask(40, me, me.handleMemberUnHoverDelayed, evt);	
	};
	
	me.handleMemberUnHoverDelayed = function(evt) {
		var me = this;
		if (!me.mouseIsOver) {
			// well, finally no nasty hover event has disturbed our good unhover
			// process, so we can assume that this is a real unhover event
			
			me.wasHovering = false;
			if ($.isFunction(me.unhoverCallback)) {
				me.unhoverCallback({ target: me.target, origEvent: evt, mouseEventGroup: me });
			}
		}
	};
		
	/*
	 * this function is used for later addition of member objects like dynamic tooltips
	 */
	me.addMember = function(mem) {
		var me = this;
		// if (me.clickCallback && noClick) $(mem).click(me.handleClick.bind(me));
		if (me.hoverCallback) $(mem).hover(me.handleMemberHover.bind(me), me.handleMemberUnHover.bind(me));
		me.members.push(mem);
	};
	
	/*
	 * this function is used for later removal of member objects like dynamic tooltips
	 */
	me.removeMember = function(mem) {
		var me = this, members = me.members, i, tmp = [];
		if (me.clickCallback) $(mem).unbind('click');
		if (me.hoverCallback) $(mem).unbind('mouseenter mouseleave');
		for (i in members) {
			if (members[i] != mem) tmp.push(members[i]);
		}
		me.members = tmp;
		
	};
};

BubbleTree.Ring = function(node, bc, o, rad, attr) {
	
	var me = this;
	me.className = "ring";
	me.rad = rad;
	me.bc = bc;
	me.attr = attr;
	me.origin = o;
	me.alpha = 1;
	me.visible = false;
	me.node = node;
	
	me.init = function() {
		//var o = me.origin;
	};
	
	me.draw = function() {
		var me = this, o = me.origin;
		if (!me.visible) return;
		me.circle.attr({ cx: o.x, cy: o.y, r: me.rad, 'stroke-opacity': me.alpha });
	};
	
	/*
	 * removes all raphael nodes from stage
	 */
	me.hide = function() {
		var me = this;
		me.circle.remove();
		me.visible = false;
	};
	
	me.show = function() {
		var me = this;
		me.circle = me.bc.paper.circle(o.x, o.y, me.rad).attr(me.attr);
		me.visible = true;
		me.circle.toBack();
	};
	
	
	me.init();
};

BubbleTree.Transitioner = function(duration) {
	
	var me = this;
	
	me.duration = duration;
	me.running = false;
	me.completeCallbacks = [];
	
	me.changeLayout = function(layout) {
		var i, o, props, p, me = this;
		me.running = true;
		me.layout = layout;
		
		// at first show all objects that are marked for showing
		for (i in layout.toShow) {
			o = layout.toShow[i];
			if ($.isFunction(o.show)) o.show();
		}
		
		for (i in layout.objects) {
			o = layout.objects[i];
			if (o === undefined || o === null) continue;
			props = layout.props[i];
			
			if (me.duration > 0) {
				var tween = new TWEEN.Tween(o), toProps = {};
				
				for (p in props) {
					//o[p] = props[p];
					toProps[p] = props[p];
				}
				tween.to(toProps, me.duration);
				tween.easing(TWEEN.Easing.Exponential.EaseOut);
				if ($.isFunction(o.draw)) tween.onUpdate(o.draw.bind(o));
				if (i == layout.objects.length-1) tween.onComplete(me._completed.bind(me));
				tween.start();
			} else {
				for (p in props) {
					o[p] = props[p];
				}
				if (o && $.isFunction(o.draw)) o.draw();
			}
		}
		if (me.duration === 0) {
			// redraw all
			for (i in layout.objects) {
				o = layout.objects[i];
				if (o && $.isFunction(o.draw)) o.draw();
			}
			me._completed();
		}
	};
	
	me.onComplete = function(callback) {
		var me = this;
		try {
			if ($.isFunction(callback)) me.completeCallbacks.push(callback);
		} catch (e) {
			//vis4.log(e);
		}
	};
	
	me._completed = function() {
		var me = this, callbacks = me.completeCallbacks, i, obj;
		me.running = false;
		
		for (i in me.layout.objects) {
			obj = me.layout.objects[i];
			if (obj && $.isFunction(obj.draw)) obj.draw(); // the final draw	
		}
		// now hide all objects marked for hiding
		for (i in me.layout.toHide) {
			obj = me.layout.toHide[i];
			if (obj && $.isFunction(obj.hide)) obj.hide();
		}
		
		for (i in callbacks) {
			callbacks[i]();
		}
	};
	
};/*jshint undef: true, browser:true, jquery: true, devel: true */
/*global Raphael, TWEEN, BubbleTree */

BubbleTree.Utils = {};

BubbleTree.Utils.log = function() {
	try {
		if (window.hasOwnProperty('console')) console.log.apply(this, arguments);
	} catch (e) {}	
};

BubbleTree.Utils.amount2rad = function(a) {
	return Math.pow(Math.max(0, a) /BubbleTree.a2radBase, 0.6);
};

BubbleTree.Utils.formatNumber = function(n) {

/*
	var prefix = '';
	if (n < 0) {
		n = n*-1;
		prefix = '-';
	}
	if (n >= 1000000000000) return prefix+Math.round(n / 100000000000)/10 + 't';
	if (n >= 1000000000) return prefix+Math.round(n / 100000000)/10 + 'b';
	if (n >= 1000000) return prefix+Math.round(n / 100000)/10 + 'm';
	if (n >= 1000) return prefix+Math.round(n / 100)/10 + 'k';
	else return prefix+n;
*/
    return n/10000;

};

BubbleTree.Vector = function(x,y) {
	var me = this;
	me.x = x; 
	me.y = y;
	
	/*
	 * calculates the length of the vector
	 */
	me.length = function() {
		var me = this;
		return Math.sqrt(me.x*me.x + me.y * me.y);
	};
	
	/*
	 * changes the length of the vector
	 */
	me.normalize = function(len) {
		var me = this, l = me.length();
		if (!len) len = 1.0;
		me.x *= len/l;
		me.y *= len/l;
	};
	
	/*
	 * creates an exact copy of this vector
	 */
	me.clone = function() {
		var me = this;
		return new BubbleTree.Vector(me.x, me.y);
	};
};/*jshint undef: true, browser:true, jquery: true, devel: true, smarttabs: true */
/*global Raphael, TWEEN, BubbleTree, vis4 */
BubbleTree.Bubbles = BubbleTree.Bubbles || {};
BubbleTree.Bubbles.Plain = function(node, bubblechart, origin, radius, angle, color) {

	var ns = BubbleTree, utils = ns.Utils, me = this;
	me.className = "bubble";
	me.node = node;
	me.paper = bubblechart.paper;
	me.origin = origin;
	me.bc = bubblechart;
	me.rad = radius;
	me.angle = angle;
	me.color = color;
	me.alpha = 1;
	me.visible = false;
	me.ns = ns;
	me.pos = ns.Vector(0,0);
/* Tim changed the below on 20 August 2015. */
	me.bubbleRad = 80// utils.amount2rad(this.node.amount);
	me.container = me.bc.$container;
	
	/*
	 * child rotation is just used from outside to layout possible child bubbles
	 */
	me.childRotation = 0;
	
	
	/*
	 * convertes polar coordinates to x,y
	 */
	me.getXY = function() {
		var me = this, o = me.origin, a = me.angle, r = me.rad;
		if (me.pos === undefined) me.pos = new me.ns.Vector(0,0);
		me.pos.x = o.x + Math.cos(a) * r;
		me.pos.y = o.y - Math.sin(a) * r;
	};
	
	/*
	 * initializes the bubble
	 */
	me.init = function() {
		var me = this;
		me.getXY();
		
		var showIcon = false; //this.bubbleRad * this.bc.bubbleScale > 30;
		
		if (!me.node.shortLabel) me.node.shortLabel = me.node.label.length > me.bc.config.cutLabelsAt+3 ? me.node.label.substr(0, me.bc.config.cutLabelsAt)+'...' : me.node.label;
		
		me.initialized = true;
		
		//me.show();
	};
	
	/*
	 *
	 */
	me.onclick = function(e) {
		var me = this;
		me.bc.onNodeClick(me.node);
		
		//if (me.node.children.length > 1) {
			me.bc.navigateTo(me.node);
		//}
	};
	
	me.onhover = function(e) {
		var me = this, c = me.bc.$container[0];
		e.node = me.node;
		e.target = me;
		e.bubblePos = { x:me.pos.x, y: me.pos.y };
		e.mousePos = { x:e.origEvent.pageX - c.offsetLeft, y: e.origEvent.pageY - c.offsetTop };
		e.type = 'SHOW';
		me.bc.tooltip(e);
	};
	
	me.onunhover = function(e) {
		var me = this, c = me.bc.$container[0];
		e.node = me.node;
		e.type = 'HIDE';
		e.target = me;
		e.bubblePos = { x:me.pos.x, y: me.pos.y };
		e.mousePos = { x:e.origEvent.pageX - c.offsetLeft, y: e.origEvent.pageY - c.offsetTop };
		me.bc.tooltip(e);
	};
	
	me.draw = function() {
		var me = this,
			r = Math.max(5, me.bubbleRad * me.bc.bubbleScale),
			ox = me.pos.x,
			oy = me.pos.y,
			devnull = me.getXY(),
		x = me.pos.x, y = me.pos.y;
		if (!me.visible) return;
		
		me.circle.attr({ cx: me.pos.x, cy: me.pos.y, r: r, 'fill-opacity': me.alpha });
		if (me.node.children.length > 1) me.dashedBorder.attr({ cx: me.pos.x, cy: me.pos.y, r: r-4, 'stroke-opacity': me.alpha * 0.9 });
		else me.dashedBorder.attr({ 'stroke-opacity': 0 });
		

		//me.label.attr({ x: me.pos.x, y: me.pos.y, 'font-size': Math.max(4, me.bubbleRad * me.bc.bubbleScale * 0.25) });
		
		me.label.show();
		me.label.find('*').show();
		me.label2.show();
		if (r >= me.bc.config.minRadiusLabels) {
			// full label
			me.label2.hide();
		} else if (r >= me.bc.config.minRadiusAmounts) {
			// full label
			me.label.find('.desc').hide();
		} else if (r >= me.bc.config.minRadiusHideLabels) {
			me.label.hide();
		} else {
			me.label.hide();
			me.label2.hide();
		}

		me.label.css({ width: 2*r+'px', opacity: me.alpha });
		me.label.css({ left: (me.pos.x-r)+'px', top: (me.pos.y-me.label.height()*0.5)+'px' });
	
		var w = Math.max(70, 3*r);
		me.label2.css({ width: w+'px', opacity: me.alpha });
		me.label2.css({ left: (x - w*0.5)+'px', top: (y + r)+'px' });

		//if (me.icon) me.icon.translate(me.pos.x - ox, me.pos.y - oy);
	
	};
	
	/*
	 * removes all visible elements from the page
	 */
	me.hide = function() {
		var me = this, i;
		me.circle.remove();
		me.dashedBorder.remove();
		me.label.remove();
		me.label2.remove();
		
		//$('#bubble-chart')
		me.visible = false;

		
		//if (me.icon) me.icon.remove();
	};
	
	/*
	 * adds all visible elements to the page
	 */
	me.show = function() {
		var me = this, i, cx = me.pos.x, cy = me.pos.y, r = Math.max(5, me.bubbleRad * me.bc.bubbleScale);
		
		me.circle = me.paper.circle(cx, cy, r)
			.attr({ stroke: false, fill: me.color });

		me.dashedBorder = me.paper.circle(cx, cy, r-3)
			.attr({ stroke: '#ffffff', 'stroke-dasharray': "- " });
	

		me.label = $('<div class="label '+me.node.id+'"><div class="amount">'+utils.formatNumber(me.node.amount)+'</div><div class="desc">'+me.node.shortLabel+'</div></div>');
		me.container.append(me.label);
		
		if (me.node.children.length > 0) {
			$(me.circle.node).css({ cursor: 'pointer'});
			$(me.label).css({ cursor: 'pointer'});
		}	
		
		// additional label
		me.label2 = $('<div class="label2 '+me.node.id+'"><span>'+me.node.shortLabel+'</span></div>');
		me.container.append(me.label2);
		
		var list = [me.circle.node, me.label, me.dashedBorder.node];

		var mgroup = new me.ns.MouseEventGroup(me, list);
		mgroup.click(me.onclick.bind(me));
		mgroup.hover(me.onhover.bind(me));
		mgroup.unhover(me.onunhover.bind(me));
		
		me.visible = true;
		
	};
	
	/*
	 * adds an invisible bubble on top for seamless 
	 * event handling
	 */
	me.addOverlay = function() {
		// add invisible overlay circle
		var me = this;
		
		me.overlay = me.paper.circle(me.circle.attrs.cx, me.circle.attrs.cy, me.circle.attrs.r)
			.attr({ stroke: false, fill: '#fff', 'opacity': 0});
		
		if (Raphael.svg) {
			me.overlay.node.setAttribute('class', me.node.id);
		}
		$(me.overlay.node).css({ cursor: 'pointer'});
		$(me.overlay.node).click(me.onclick.bind(me));
		
		$(me.label).click(me.onclick.bind(me));
	};
	
	me.init();
};
/*jshint undef: true, browser:true, jquery: true, devel: true, smarttabs: true */
/*global Raphael, TWEEN, BubbleTree, vis4 */

BubbleTree.Styles = {};
/*jshint undef: true, browser:true, jquery: true, devel: true, smarttabs: true */
/*global Raphael, TWEEN, BubbleTree */

/*
 * stores visual attributes of all elements in the visualization
 * 
 */
BubbleTree.Layout = function() {

	var me = this;
	me.objects = [];
	me.props = [];
	me.toHide = [];
	me.toShow = [];
	
	/*
	 * flare-style transitioner syntax
	 *
	 * if you have an object bubble, you can easily change its properties with
	 * 
	 * var l = new OpenSpendings.BubbleTree.Layout();
	 * l.$(bubble).radius = 30;
	 * l.$(bubble).angle = 3.14;
	 */
	me.$ = function(obj) {
		var me = this, i, o, p;
		for (i in me.objects) {
			o = me.objects[i];
			if (o == obj) return me.props[i];
		}
		me.objects.push(obj);
		p = {};
		me.props.push(p);
		return p;
	};
	
	/*
	 * use me function to mark objects that should be shown before
	 * the transition
	 */
	me.show = function(obj) {
		var me = this;
		me.toShow.push(obj);
	};
	
	
	/*
	 * use me function to mark objects that should be hidden after
	 * the transition
	 */
	me.hide = function(obj) {
		var me = this;
		me.toHide.push(obj);
	};
};
