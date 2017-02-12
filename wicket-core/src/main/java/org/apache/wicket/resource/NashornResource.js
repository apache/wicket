// loading escodegen.js from classpath
load("classpath:org/apache/wicket/resource/escodegen.browser"
		+ (debug ? "" : ".min") + ".js");

// load parser.js from nashorn resources
load("nashorn:parser.js");

var astObject = parse(script);
if (debug) {
	print(debug_log_prefix + "Original Script: " + script);
	print(debug_log_prefix + "Original Script AST: "
			+ JSON.stringify(astObject));
}

function getAbortStatement(type) {
	var abortStatement = {
		"type" : "IfStatement",
		"test" : {
			"type" : "CallExpression",
			"callee" : {
				"type" : "Identifier",
				"name" : "nashornResourceReferenceScriptExecutionThread.isInterrupted"
			},
			"arguments" : [

			]
		},
		"consequent" : {
			"type" : "BlockStatement",
			"body" : []
		},
		"alternate" : null
	};

	if (type === "function") {
		abortStatement.consequent.body.unshift({
			"type" : "ReturnStatement",
			"argument" : null
		});
	} else {
		abortStatement.consequent.body.unshift({
			"type" : "BreakStatement",
			"label" : null
		});
	}
	if (debug) {
		abortStatement.consequent.body
				.unshift({
					"type" : "ExpressionStatement",
					"expression" : {
						"type" : "CallExpression",
						"callee" : {
							"type" : "Identifier",
							"name" : "print"
						},
						"arguments" : [ {
							"type" : "Literal",
							"value" : debug_log_prefix
									+ "Abort thread execution and skip all loop / function calls"
						} ]
					}
				});
	}
	return abortStatement;
}

// manipulate ast
function manipulateTree(astObject) {
	var targetAstObject;
	for ( var property in astObject) {
		var node = astObject[property];
		if (typeof node == 'object') {
			if (node == null) {
				// skip nulls
				continue;
			}
			if (Array.isArray(node) && node.length == 0) {
				// skip empty arrays
				continue;
			}
			if (node.type == 'WhileStatement'
					|| node.type == 'DoWhileStatement'
					|| node.type == 'ForStatement') {
				node.body.body.unshift(getAbortStatement("loop"));
			} else if (node.type == 'FunctionDeclaration') {
				node.body.body.unshift(getAbortStatement("function"));
			} else {
				manipulateTree(node);
			}
		}
	}
}

manipulateTree(astObject);

// generate the script again
var saveScript = escodegen.generate(astObject);

if (debug) {
	print(debug_log_prefix + "Save Script AST: " + JSON.stringify(astObject));
	print(debug_log_prefix + "Save Script: " + saveScript);
}

// return value
saveScript;