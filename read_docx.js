const mammoth = require(process.env.TEMP + '\\docx-reader\\node_modules\\mammoth');
const path = 'C:\\Users\\16674\\Desktop\\软创文件\\AI医疗便民小助手项目架构说明书.docx';

mammoth.extractRawText({path: path})
    .then(result => {
        console.log(result.value);
    })
    .catch(err => {
        console.error('Error:', err);
    });
