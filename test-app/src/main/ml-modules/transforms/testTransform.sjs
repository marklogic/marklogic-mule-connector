function testTransform(context, params, content)
{
  if (context.inputType.search('json') >= 0) {
    let obj = {};
    obj[params["hello"]] = params["world"];
    return obj;
  } else {
    /* Pass thru for non-JSON documents */
    return content;
  }
};

exports.transform = testTransform;
