const fs = require('fs');
const path = require('path');
const { XMLParser } = require('fast-xml-parser');

// 언어별 values 폴더 매핑
const LANG_MAP = {
  en: 'values',
  ko: 'values-ko',
  ja: 'values-ja',
};

const inputPath = path.join(__dirname, 'strings-master.xml');
const outputBase = __dirname

// XML 파서 설정
const parser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: '',
  preserveOrder: false
});

// 파일 읽기 및 파싱
const xmlData = fs.readFileSync(inputPath, 'utf-8');
const parsed = parser.parse(xmlData);
const strings = parsed.resources.string;

const localized = { en: [], ko: [], ja: [] };

// 각 문자열 항목 분류
strings.forEach(item => {
  const name = item.name;
  for (const lang in LANG_MAP) {
    if (item[lang]) {
      localized[lang].push({
        name,
        value: item[lang]
      });
    }
  }
});

// 언어별 strings.xml 생성
for (const lang in localized) {
  const dir = path.join(outputBase, LANG_MAP[lang]);
  const filepath = path.join(dir, 'strings.xml');

  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });

  const content = [
    '<?xml version="1.0" encoding="utf-8"?>',
    '<resources>'
  ];

  localized[lang].forEach(({ name, value }) => {
    content.push(`  <string name="${name}">${value}</string>`);
  });

  content.push('</resources>');

  fs.writeFileSync(filepath, content.join('\n'), 'utf-8');
  console.log(`✅ Generated: ${filepath}`);
}
