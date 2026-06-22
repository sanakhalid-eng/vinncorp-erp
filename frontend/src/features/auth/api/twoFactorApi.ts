import API from '../../../api/axios';

/**
 * @returns {Promise<import('../types/api').ApiResponse<import('../types/api').TwoFactorSetupData>>}
 */
export const setupTwoFactor = async () => {
  try {
    const res = await API.post('/2fa/setup');
    return res.data; // Already { success, message, data: { secretKey, qrCodeUrl, totpAuthUrl } }
  } catch (error) {
    console.error('2FA setup error:', error);
    throw error;
  }
};

/**
 * @returns {Promise<import('../types/api').ApiResponse<import('../types/api').TwoFactorVerifyResult>>}
 */
export const verifyTwoFactorSetup = async (code: any) => {
  try {
    const res = await API.post('/2fa/verify-setup', { code });
    return res.data; // { success, message, data: { backupCodes, message } }
  } catch (error) {
    console.error('2FA verify setup error:', error);
    throw error;
  }
};

/**
 * @returns {Promise<import('../types/api').ApiResponse<import('../types/api').TwoFactorStatus>>}
 */
export const getTwoFactorStatus = async () => {
  try {
    const res = await API.get('/2fa/status');
    return res.data; // { success, message, data: { enabled } }
  } catch (error) {
    console.error('2FA status error:', error);
    throw error;
  }
};

export const disableTwoFactor = async () => {
  try {
    const res = await API.post('/2fa/disable');
    return res.data;
  } catch (error) {
    console.error('2FA disable error:', error);
    throw error;
  }
};

/**
 * @returns {Promise<import('../types/api').ApiResponse<import('../types/api').AuthResponse>>}
 */
export const validateTwoFactor = async (identifier: any, code: any) => {
  try {
    const res = await API.post('/2fa/validate', { identifier, code });
    return res.data;
  } catch (error) {
    console.error('2FA validate error:', error);
    throw error;
  }
};
