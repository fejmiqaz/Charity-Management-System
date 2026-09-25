import {formatDate, translate} from './index.js';
// The API currently supplies English notification templates rather than message keys.
// Match only those complete templates; names and descriptions remain user content.
export function notificationTitle(language, title) {
  const match = /^(Task assigned|Upcoming event): ([\s\S]+)$/.exec(title || '');
  return match ? translate(language, `${match[1]}: {title}`, {title: match[2]}) : title;
}
function notificationDate(language, value) {
  const match = /^(\d{2}) ([A-Za-z]{3}) (\d{4}), (\d{2}:\d{2}) \(([^)]+)\)$/.exec(value);
  if (!match) return translate(language, value);
  const month = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'].indexOf(match[2]) + 1;
  if (!month) return value;
  return `${formatDate(language, `${match[3]}-${String(month).padStart(2,'0')}-${match[1]}T${match[4]}`, {dateStyle:'medium', timeStyle:'short'})} (${match[5]})`;
}
export function notificationMessage(language, message) {
  if (typeof message !== 'string') return message;
  const assignment = /^You have been assigned to "([\s\S]+)" for ([\s\S]+)\.\nEvent: ([^\n]+)(?:\nTask details: ([\s\S]*))?$/.exec(message);
  if (assignment) return [translate(language, 'You have been assigned to "{task}" for {event}.', {task:assignment[1],event:assignment[2]}), translate(language,'Event: {date}',{date:notificationDate(language,assignment[3])}), ...(assignment[4] ? [translate(language,'Task details: {description}',{description:assignment[4]})] : [])].join('\n');
  const reminder = /^([\s\S]+) is (tomorrow|in (\d+) days)\.\nScheduled for ([^\n]+)\.$/.exec(message);
  if (reminder) return translate(language, reminder[3] ? '{event} is in {days} days.' : '{event} is tomorrow.', {event:reminder[1],days:reminder[3]}) + '\n' + translate(language,'Scheduled for {date}.',{date:notificationDate(language,reminder[4])});
  return message;
}
